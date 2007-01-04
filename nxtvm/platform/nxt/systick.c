/*
 *  This provides a 1000Hz tick for the system.
 *
 *  NB At 1000Hz, a U32 will roll over in approx 50 days.
 *  Therefore a todo to get rid of this at some stage.
 *
 *  We're using TC0
 */
 
#include "aic.h"
#include "AT91SAM7.h"
#include "interrupts.h"
#include "systick.h"

#include "nxt_motors.h"
#include "nxt_avr.h"

extern volatile unsigned char gMakeRequest;

#define PIT_FREQ 1000  /* Hz */

#define LOW_PRIORITY_IRQ 10

static volatile U32 systick_sec;
static volatile U32 systick_sub_sec;
static volatile U32 systick_ms;

extern void systick_isr_entry(void);
extern void systick_low_priority_entry(void);

// Systick low priority
void systick_low_priority_C(void)
{
  *AT91C_AIC_ICCR = (1<<LOW_PRIORITY_IRQ);
  gMakeRequest = 1; // trigger Java tick request
  nxt_avr_1kHz_update();
  nxt_motor_1kHz_process();
}

// Called at 1000Hz
void systick_isr_C(void)
{
  U32 status;
  
  /* Read status to confirm interrupt */
  status = *AT91C_PITC_PIVR;
  
//  systick_low_priority_C();
  
  systick_ms++;
  
  systick_sub_sec++;
  
  if(systick_sub_sec >=  PIT_FREQ)
  {
    systick_sub_sec = 0;
    systick_sec++;
  }
  
  // Trigger low priority task
  *AT91C_AIC_ISCR = (1<<LOW_PRIORITY_IRQ);
}



U32 systick_get_ms(void)
{
  // We're using a 32-bitter and can assume that we
  // don't need to do any locking here.
  return systick_ms ;  
}


void systick_wait_ms(U32 ms)
{
  volatile U32 final= ms + systick_ms;
  
  while(systick_ms < final) {}
}


void systick_wait_ns(U32 ns)
{
  volatile x = (ns>>7) + 1;
  
  while(x){
    x--;
  }
}

void systick_init(void)
{
  int i_state = interrupts_get_and_disable();
  
  aic_mask_off(LOW_PRIORITY_IRQ);
  aic_set_vector(LOW_PRIORITY_IRQ,
                 (1<<5) /* positive internal edge */ |
                 AIC_INT_LEVEL_LOW,
                 (U32)systick_low_priority_entry);
  aic_mask_on(LOW_PRIORITY_IRQ);
  
  aic_mask_off(AT91C_PERIPHERAL_ID_SYSIRQ);
  aic_set_vector(AT91C_PERIPHERAL_ID_SYSIRQ,
                 (1<<5) /* positive internal edge */ |
                 AIC_INT_LEVEL_NORMAL,
                 (U32)systick_isr_entry);
                 
  aic_mask_on(AT91C_PERIPHERAL_ID_SYSIRQ);
  *AT91C_PITC_PIMR = ((CLOCK_FREQUENCY/16/PIT_FREQ)-1) | 
                      0x03000000; /* Enable, enable interrupts */
                      
  if(i_state)
    interrupts_enable();
}

void systick_get_time(U32 *sec, U32 *usec)
{
  int istate = interrupts_get_and_disable();
  
  if(sec)  *sec = systick_sec;
  if(usec) *usec = systick_sub_sec * (1000000/PIT_FREQ);
  
  if(istate) interrupts_enable();
}


static U32 test_counter;
void systick_test(void)
{
  while(1){
    test_counter++;
    systick_wait_ms(2000);
  }
}
