package frc.robot.subsystem;

/*
This should have 2 (private) EncoderMotors to set and measure the speed
The constructor should simply initialize them without taking any input to keep the motor ids in this class
The ids will be available later, I'll let whoever's working on this know them, just set them both to 0 for now
Also fyi the motors should both be CANDriveMotorPairs with TalonSRXs
It should also have a controlled state where the controllers are used to control it
For auto and manual usage, it would also be useful to make public methods to set the speed of each motor individually
These methods should be used to set the speed from then on, as rate limiting will eventually be added
Also, they will be useful to easily switch between closed loop and percent output in case something goes wrong
We'll probably get a gyro on the robot later, so that will have to be added eventually (so look at the 2019 code to prepare)
There should be getVelocity methods to measure the velocity which will be sent to the dashboard by other code
check https://github.com/FRobotics/robot-2019 for the drive logic
 */

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Robot;
import frc.robot.RobotMode;
import frc.robot.input.Axis;
import frc.robot.input.Button;
import frc.robot.input.Controller;
import frc.robot.subsystem.base.StateInstance;
import frc.robot.subsystem.base.Subsystem;
import frc.robot.subsystem.base.motor.CANDriveMotorPair;
import frc.robot.subsystem.base.motor.EncoderMotor;

import java.util.Arrays;
import java.util.List;

public class DriveTrain extends Subsystem<DriveTrain.State> {

    public enum State {
        DISABLED,
        CONTROLLED,
        TEST1,
        TEST2
    }

    public List<StateInstance<State>> TEST = Arrays.asList(
            new StateInstance<>(State.TEST1, 250),
            new StateInstance<>(State.TEST2, 250)
    );

    // TODO: real motor ids
    private EncoderMotor leftMotor = new CANDriveMotorPair(new TalonSRX(14), new TalonSRX(13));
    private EncoderMotor rightMotor = new CANDriveMotorPair(new TalonSRX(10), new TalonSRX(12)).invert();

    public DriveTrain() {
        super("driveTrain", State.DISABLED);
    }

    public void setLeftVelocity(double velocity) {
        this.leftMotor.setVelocity(velocity);
    }

    public void setRightVelocity(double velocity) {
        this.rightMotor.setVelocity(velocity);
    }

    public void setVelocity(double velocity) {
        setLeftVelocity(velocity);
        setRightVelocity(velocity);
    }

    @Override
    public void onInit(RobotMode mode) {
        this.clearStateQueue();
        switch(mode) {
            default:
            case DISABLED:
                this.setStateAndDefault(State.DISABLED);
                break;
            case TELEOP:
                this.setStateAndDefault(State.CONTROLLED);
                break;
        }
    }

    @Override
    public void handleState(Robot robot, State state) {
        switch(state) {
            case DISABLED:
                setVelocity(0);
                break;
            case CONTROLLED:
                Controller controller = robot.getMovementController();

                // person.arm.moveUp(3);

                double leftY = -controller.getAxis(Axis.LEFT_Y);
                double rightY = -controller.getAxis(Axis.RIGHT_Y);

                setLeftVelocity(leftY*5);
                setRightVelocity(rightY*5);

                if(controller.buttonPressed(Button.A)) {
                    setStateQueue(TEST);
                }

                break;
            case TEST1:
                setVelocity(-3);
                break;
            case TEST2:
                setVelocity(3);
                break;
        }
    }

    @Override
    public void updateDashboard() {
        SmartDashboard.putNumber(dashKey("leftVelocity"), this.leftMotor.getVelocity());
    }
}
