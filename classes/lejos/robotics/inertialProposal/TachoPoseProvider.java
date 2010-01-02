package lejos.robotics.inertialProposal;

import lejos.geom.Point;
import lejos.robotics.Move;
import lejos.robotics.Move.MoveType;
import lejos.robotics.MoveListener;
import lejos.robotics.MoveProvider;
import lejos.robotics.Pose;
import lejos.robotics.TachoMotor;
import lejos.robotics.localization.PoseProvider;

public class TachoPoseProvider implements PoseProvider, MoveListener
{
    private lejos.robotics.Move currentMove;
    private Regulator reg = new Regulator();
    private float wheelDiameter;
    private float wheelRatio;
    private TachoMotor leftMotor;
    private TachoMotor rightMotor;
    private Pose pose = new Pose();

    public TachoPoseProvider(TachoMotor leftMotor, TachoMotor rightMotor, float wheelDiameter, float wheelBase)
    {
        this.leftMotor = leftMotor;
        this.rightMotor = rightMotor;
        this.wheelDiameter = wheelDiameter;
        this.wheelRatio = wheelBase / wheelDiameter;
    }

    public Pose getPose()
    {
        return pose;
    }

    public void setMoveProvider(MoveProvider mp)
    {
        mp.addMoveListener(this);
        reg.start();
    }

    public void updateLocation(Point location)
    {
        pose.getLocation().x = location.x;
        pose.getLocation().y = location.y;
    }

    public void updateHeading(float heading)
    {
        pose.setHeading(heading);
    }

    public void moveStarted(Move event, MoveProvider mp)
    {
        synchronized(this)
        {
            currentMove = event;
        }
    }

    public void moveStopped(Move event, MoveProvider mp)
    {
        synchronized(this)
        {
            currentMove = null;
        }
    }

    public lejos.robotics.Move CurrentMove()
    {
        synchronized(this)
        {
            return currentMove;
        }
    }

    private class Regulator extends Thread
    {
        public Regulator()
        {
            setDaemon(true);
        }

        @Override
        public void run()
        {
            long lastUpdate = System.currentTimeMillis();
            lejos.robotics.Move cm;
            while (true)
            {
                // Check time
                Thread.yield();
                long now = System.currentTimeMillis();
                if(now - lastUpdate == 0)
                    continue;
                cm = CurrentMove();
                if(cm == null)
                    continue;

                // Rotate or travel
                if(cm.getMoveType() == MoveType.ROTATE)
                    WaitRotateComplete();
                if(cm.getMoveType() == MoveType.TRAVEL)
                    WaitTravelComplete();
                
                // Move on
                lastUpdate = now;
            }

        }

        private void WaitRotateComplete()
        {
            lejos.robotics.Move cm = CurrentMove();
            leftMotor.resetTachoCount();
            rightMotor.resetTachoCount();
            int leftCount = 0;
            int rightCount = 0;
            float oldHeading = pose.getHeading();
            while(cm != null && cm.getMoveType() == MoveType.ROTATE)
            {
                // Read tacho's
                leftCount = leftMotor.getTachoCount();
                rightCount = rightMotor.getTachoCount();
                float thisHeading = (float)(rightCount - leftCount) / wheelRatio / 2.0f;
                pose.setHeading(oldHeading + thisHeading);

                // Move on
                Thread.yield();
                cm = CurrentMove();
            }
        }

        private void WaitTravelComplete()
        {
            Point dir = new Point(0, 0);
            int lastLeftCount = 0;
            lastLeftCount = leftMotor.getTachoCount();
            lejos.robotics.Move cm = CurrentMove();
            while(cm != null && cm.getMoveType() == MoveType.TRAVEL)
            {
                // Read tacho's
                int leftCount = leftMotor.getTachoCount();
                long tachoDiff = leftCount - lastLeftCount;
                float deltaDist = (float)tachoDiff / 360F * wheelDiameter * (float)Math.PI;

                // Update position
                dir.x = (float)Math.cos(Math.toRadians(pose.getHeading())) * deltaDist;
                dir.y = -(float)Math.sin(Math.toRadians(pose.getHeading())) * deltaDist;
                pose.getLocation().x += dir.x;
                pose.getLocation().y += dir.y;

                // Move on
                lastLeftCount = leftCount;
                Thread.yield();
                cm = CurrentMove();
            }
        }

    }

}