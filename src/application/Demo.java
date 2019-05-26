package application;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import threads.TrackRec;

import com.kuka.common.ThreadUtil;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;

import com.kuka.roboticsAPI.conditionModel.ConditionObserver;
import com.kuka.roboticsAPI.conditionModel.ForceCondition;
import com.kuka.roboticsAPI.conditionModel.ICondition;
import com.kuka.roboticsAPI.conditionModel.JointTorqueCondition;
import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.deviceModel.JointEnum;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.executionModel.IFiredConditionInfo;
import com.kuka.roboticsAPI.geometricModel.CartDOF;
import com.kuka.roboticsAPI.geometricModel.CartPlane;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.geometricModel.World;
import com.kuka.roboticsAPI.geometricModel.math.CoordinateAxis;
import com.kuka.roboticsAPI.motionModel.IMotion;
import com.kuka.roboticsAPI.motionModel.IMotionContainer;
import com.kuka.roboticsAPI.motionModel.MotionBatch;
import com.kuka.roboticsAPI.motionModel.RobotMotion;
import com.kuka.roboticsAPI.motionModel.Spline;
import com.kuka.roboticsAPI.motionModel.controlModeModel.CartesianImpedanceControlMode;
import com.kuka.roboticsAPI.motionModel.controlModeModel.CartesianSineImpedanceControlMode;
import com.kuka.roboticsAPI.motionModel.controlModeModel.JointImpedanceControlMode;
import com.kuka.roboticsAPI.motionModel.controlModeModel.PositionControlMode;
import com.kuka.roboticsAPI.uiModel.ApplicationDialogType;

/**
 * Implementation of a robot application.
 * <p>
 * The application provides a {@link RoboticsAPITask#initialize()} and a 
 * {@link RoboticsAPITask#run()} method, which will be called successively in 
 * the application lifecycle. The application will terminate automatically after 
 * the {@link RoboticsAPITask#run()} method has finished or after stopping the 
 * task. The {@link RoboticsAPITask#dispose()} method will be called, even if an 
 * exception is thrown during initialization or run. 
 * <p>
 * <b>It is imperative to call <code>super.dispose()</code> when overriding the 
 * {@link RoboticsAPITask#dispose()} method.</b> 
 * 
 * @see UseRoboticsAPIContext
 * @see #initialize()
 * @see #run()
 * @see #dispose()
 */
public class Demo extends RoboticsAPIApplication {
	@Inject
	private LBR lBR_iiwa_7_R800_1;
	@Inject
	private Controller kuka_Sunrise_Cabinet_1;

	private Tool tool;

	public ArrayList<Frame> _trackPoints;
	private ArrayList<Spline> _track;
	private ConditionObserver trackServer;

	private ICondition forceCon;
	private CartesianImpedanceControlMode soft;
	private Frame frames[];
	private MotionBatch mb[];
	private ObjectFrame world;
	private ObjectFrame actTCP;
	private ForceCondition gestureForce;
	private double gestureForceVal;

	@Override
	public void initialize() {
		tool = createFromTemplate("Pin");
		tool.attachTo(lBR_iiwa_7_R800_1.getFlange());
		actTCP = tool.getFrame("/Pin_TCP");

		_track = new ArrayList<Spline>();
		_trackPoints = new ArrayList<Frame>();

		// Abbruchbedingung für eine Kollision in grauer Technik
		forceCon = defineSensitivity();

		soft = new CartesianImpedanceControlMode();
		soft.parametrize(CartDOF.ALL).setDamping(.7);
		soft.parametrize(CartDOF.ROT).setStiffness(100);
		soft.parametrize(CartDOF.TRANSL).setStiffness(600);

		// World-Frame
		world = World.Current.getRootFrame();
		lBR_iiwa_7_R800_1.setESMState("2");

		// Force condition for gesture control
		gestureForceVal = getApplicationData().getProcessData("gestureForce")
				.getValue();

		gestureForce = ForceCondition.createNormalForceCondition(
				lBR_iiwa_7_R800_1.getFlange(), CoordinateAxis.Y,
				gestureForceVal);
	}

	private void teachMe() {

		// Initialize local selection variables and number of points to be
		// taught
		int sel = -1;
		int number_of_points = getApplicationData().getProcessData("teachP")
				.getValue();
		;
		frames = new Frame[number_of_points];
		mb = new MotionBatch[number_of_points];
		DecimalFormat ZweiNach = new DecimalFormat("###0.00");

		// Move to initial position and clear all existing points in array ->
		// make them the current position
		actTCP.move(ptp(getApplicationData().getFrame("/Demo/startP"))
				.setJointVelocityRel(0.1));
		for (int i = 0; i < number_of_points; i++) {
			frames[i] = lBR_iiwa_7_R800_1
					.getCurrentCartesianPosition(lBR_iiwa_7_R800_1.getFlange());
			mb[i] = new MotionBatch(ptp(frames[i]));
		}

		// Start Impedance Mode
		JointImpedanceControlMode impMode = new JointImpedanceControlMode(1.0,
				7.0, 7.0, 7.0, 7.0, 6.0, 1.0);
		IMotionContainer handle;
		handle = lBR_iiwa_7_R800_1.moveAsync(positionHold(impMode, -1,
				TimeUnit.SECONDS));

		int count = 0;
		while (sel != 2 & count != number_of_points) {

			sel = getApplicationUI().displayModalDialog(
					ApplicationDialogType.QUESTION, "what to do?", "LIN - 0",
					"PTP - 1", "Ende - 2");

			switch (sel) {
			case 0:
				if (count < number_of_points) {
					frames[count] = lBR_iiwa_7_R800_1
							.getCurrentCartesianPosition(lBR_iiwa_7_R800_1
									.getFlange());
					mb[count] = new MotionBatch(lin(frames[count]));
					System.out.println("Punkt: "
							+ count
							+ ", Basis: World, Bewegung: LIN\n"
							+ "X = "
							+ ZweiNach.format(new Double(frames[count].getX()))
							+ " mm | "
							+ "Y = "
							+ ZweiNach.format(new Double(frames[count].getY()))
							+ " mm | "
							+ "Z = "
							+ ZweiNach.format(new Double(frames[count].getZ()))
							+ " mm\n"
							+ "A = "
							+ ZweiNach.format(new Double(Math
									.toDegrees(frames[count].getAlphaRad())))
							+ "° | "
							+ "B = "
							+ ZweiNach.format(new Double(Math
									.toDegrees(frames[count].getBetaRad())))
							+ "° | "
							+ "C = "
							+ ZweiNach.format(new Double(Math
									.toDegrees(frames[count].getGammaRad())))
							+ "°");
					count++;
					break;
				}
			case 1:
				if (count < number_of_points) {
					frames[count] = lBR_iiwa_7_R800_1
							.getCurrentCartesianPosition(lBR_iiwa_7_R800_1
									.getFlange());
					mb[count] = new MotionBatch(ptp(frames[count]));
					System.out.println("Punkt: "
							+ count
							+ ", Basis: World, Bewegung: PTP\n"
							+ "X = "
							+ ZweiNach.format(new Double(frames[count].getX()))
							+ " mm | "
							+ "Y = "
							+ ZweiNach.format(new Double(frames[count].getY()))
							+ " mm | "
							+ "Z = "
							+ ZweiNach.format(new Double(frames[count].getZ()))
							+ " mm\n"
							+ "A = "
							+ ZweiNach.format(new Double(Math
									.toDegrees(frames[count].getAlphaRad())))
							+ "° | "
							+ "B = "
							+ ZweiNach.format(new Double(Math
									.toDegrees(frames[count].getBetaRad())))
							+ "° | "
							+ "C = "
							+ ZweiNach.format(new Double(Math
									.toDegrees(frames[count].getGammaRad())))
							+ "°");
					count++;
					break;
				}
			case 2: // End Impedance control mode and return
				handle.cancel();
				return;
			default:
				break;
			}

		}
		// End Impedance control mode
		handle.cancel();

		actTCP.move(ptp(getApplicationData().getFrame("/Demo/startP"))
				.setJointVelocityRel(0.1));

		getApplicationData().getProcessData("flashingLED").setValue(true);

		getLogger().info("Für Start in Y-Richtung drücken!");
		
		actTCP.move(positionHold(new PositionControlMode(), -1,
				TimeUnit.SECONDS).breakWhen(gestureForce));
		ThreadUtil.milliSleep(700);

		getApplicationData().getProcessData("flashingLED").setValue(false);

		do {
			IMotionContainer motion;
			for (int i = 0; i < number_of_points; i++) {
				mb[i].setJointVelocityRel(0.1);
				motion = lBR_iiwa_7_R800_1.move(mb[i].breakWhen(forceCon));
				if (motion.hasFired(forceCon)) {
					// Reaktion auf Kollision
					boolean resumeMotion = behaviourAfterCollision();
					if (!resumeMotion)
						break;
				}
			}
		} while ((getApplicationUI().displayModalDialog(
				ApplicationDialogType.QUESTION, "Frames abspielen?",
				"Ja, abspielen", "Nein, abbrechen") == 0));

		actTCP.move(ptp(getApplicationData().getFrame("/Demo/startP"))
				.setJointVelocityRel(0.1));
	}

	private void recordTrack() {

		actTCP.move(ptp(getApplicationData().getFrame("/Demo/startPCustom"))
				.setJointVelocityRel(0.1));

		JointImpedanceControlMode impMode = new JointImpedanceControlMode(1.0,
				7.0, 30.0, 7.0, 7.0, 6.0, 1.0);
		TrackRec recorder = new TrackRec(lBR_iiwa_7_R800_1, tool,
				_trackPoints, getApplicationData());

		getApplicationUI().displayModalDialog(
				ApplicationDialogType.INFORMATION, "Start track recording.",
				"OK");

		//trackServer.enable();

		IMotionContainer handle;

		_trackPoints.clear();
		getLogger().info(
				"after clear: Track consists of " + _trackPoints.size()
						+ " points.");

		recorder.start();
		getApplicationData().getProcessData("trackPath").setValue(true);
		handle = actTCP.moveAsync(positionHold(impMode, -1, TimeUnit.SECONDS));

		getApplicationUI().displayModalDialog(ApplicationDialogType.WARNING,
				"Ende mit", "OK");

		getApplicationData().getProcessData("trackPath").setValue(false);
		// Start the observer
		//trackServer.disable();
		handle.cancel();

		_trackPoints = recorder.getList();
		actTCP.move(ptp(getApplicationData().getFrame("/Demo/startPCustom"))
				.setJointVelocityRel(0.1));

		// createTrack();
		getLogger().info("Track created.");

		getLogger().info("Für Start in Y-Richtung drücken!");

		getApplicationData().getProcessData("flashingLED").setValue(true);

		actTCP.move(positionHold(new PositionControlMode(), -1,
				TimeUnit.SECONDS).breakWhen(gestureForce));

		getApplicationData().getProcessData("flashingLED").setValue(false);

		do {
			double velo = getApplicationData().getProcessData("velo")
					.getValue();
			double acce = getApplicationData().getProcessData("acce")
					.getValue();
			double jerk = getApplicationData().getProcessData("jerk")
					.getValue();
			double blending = getApplicationData().getProcessData("blending")
					.getValue();

			for (int i = 1; i < _trackPoints.size(); i++) {
				getLogger().info("i=" + i);

				actTCP.moveAsync(ptp(_trackPoints.get(i))
						.setBlendingRel(blending).setJointVelocityRel(velo)
						.setJointJerkRel(jerk).setJointAccelerationRel(acce));
			}

		} while (getApplicationUI().displayModalDialog(
				ApplicationDialogType.QUESTION, "Replay Track", "OK", "Cancel") == 0);
		actTCP.move(ptp(getApplicationData().getFrame("/Demo/startPCustom"))
				.setJointVelocityRel(0.1));
	}

	private void moveCartesian(int runs) {
		getLogger().info("moveCartesian");

		// Abbruchbedingung für eine Kollision in grauer Technik
		ICondition forceCon = defineSensitivity();

		// Bewegungsprogrammierung
		actTCP.move(ptp(getFrame("/Demo/startP")).setJointVelocityRel(0.2));
		MotionBatch cart = new MotionBatch(lin(getFrame("/Demo/startP"))
				.setCartVelocity(100), linRel(-150, 0, 0).setCartVelocity(100),
				lin(getFrame("/Demo/startP")).setCartVelocity(100), linRel(0,
						150, 0).setCartVelocity(100), lin(
						getFrame("/Demo/startP")).setCartVelocity(100), linRel(
						0, 0, 150).setCartVelocity(100), lin(
						getFrame("/Demo/startP")).setCartVelocity(100))
				.breakWhen(forceCon);

		IMotionContainer motion;
		for (int i = 0; i < runs; i++) {
			motion = actTCP.move(cart);
			if (motion.hasFired(forceCon)) {
				// Reaktion auf Kollision
				boolean resumeMotion = behaviourAfterCollision();
				if (!resumeMotion)
					break;
			}
		}
	}

	private void moveNullspace(int runs) {
		getLogger().info("Nullspace Movement");

		// Abbruchbedingung für eine Kollision in grauer Technik
		ICondition forceCon = defineSensitivity();

		// Bewegungsprogrammierung
		actTCP.move(ptp(getFrame("/Demo/startP")).setJointVelocityRel(.2));
		MotionBatch ns = new MotionBatch(lin(getFrame("/Demo/startP/Links")),
				lin(getFrame("/Demo/startP/Rechts")),
				lin(getFrame("/Demo/startP/Links")),
				lin(getFrame("/Demo/startP"))).setJointVelocityRel(0.3)
				.breakWhen(forceCon);

		IMotionContainer motion;
		for (int i = 0; i < runs; i++) {
			motion = actTCP.move(ns);
			if (motion.hasFired(forceCon)) {
				// Reaktion auf Kollision
				boolean resumeMotion = behaviourAfterCollision();
				if (!resumeMotion)
					break;
			}
		}
	}

	private void stiffness() {
		getLogger().info("Stiffness");

		int answer = 0;
		double stiffX = 1000.0;
		double stiffY = 300.0;
		double stiffZ = 600.0;

		double highStiff = getApplicationData().getProcessData("highStiff")
				.getValue();
		double midStiff = getApplicationData().getProcessData("midStiff")
				.getValue();
		double lowStiff = getApplicationData().getProcessData("lowStiff")
				.getValue();

		actTCP.move(ptp(getFrame("/Demo/startP/startCompliance"))
				.setJointVelocityRel(0.2));
		CartesianImpedanceControlMode modeHandfuehren = new CartesianImpedanceControlMode();
		do {
			switch (answer) {
			case 0:
				stiffX = 1000.0;
				stiffY = 300.0;
				stiffZ = 600.0;
				break;
			case 1:
				stiffX = lowStiff;
				stiffY = lowStiff;
				stiffZ = lowStiff;
				break;
			case 2:
				stiffX = midStiff;
				stiffY = midStiff;
				stiffZ = midStiff;
				break;
			case 3:
				stiffX = highStiff;
				stiffY = highStiff;
				stiffZ = highStiff;
				break;
			}
			modeHandfuehren.parametrize(CartDOF.X).setStiffness(stiffX);
			modeHandfuehren.parametrize(CartDOF.Y).setStiffness(stiffY);
			modeHandfuehren.parametrize(CartDOF.Z).setStiffness(stiffZ);
			modeHandfuehren.parametrize(CartDOF.ROT).setStiffness(100.0);

			IMotionContainer handle;
			handle = actTCP.moveAsync(positionHold(modeHandfuehren, -1,
					TimeUnit.SECONDS));

			answer = getApplicationUI().displayModalDialog(
					ApplicationDialogType.INFORMATION,
					"Steifigkeit in X: " + stiffX + " N/m\nSteifigkeit in Y: "
							+ stiffY + " N/m\nSteifigkeit in Z: " + stiffZ
							+ " N/m", "zur Startposition fahren und Ende",
					"Weich", "Mittel", "Hart");
			handle.cancel();
			actTCP.move(ptp(getFrame("/Demo/startP/startCompliance"))
					.setJointVelocityRel(0.1));
		} while (answer != 0);
	}

	private void wiggleBounce() {
		getLogger().info("Wiggle & Bounce");

		int sel = 0;
		String actSwing = "";

		// Definition der einzelnen Modi
		// Sinus mit Frequenz 2 Hz, Amplitude 50 N, Steifigkeit 1500 [N/m]
		CartesianSineImpedanceControlMode shakeSinX;
		shakeSinX = CartesianSineImpedanceControlMode.createSinePattern(
				CartDOF.X, 2, 50, 1500);
		shakeSinX.parametrize(CartDOF.ALL).setDamping(0.7);

		// Sinus mit Frequenz 5 Hz, Amplitude 5 Nm, Steifigkeit 15 [Nm/rad]
		CartesianSineImpedanceControlMode shakeSinA;
		shakeSinA = CartesianSineImpedanceControlMode.createSinePattern(
				CartDOF.A, 5, 5, 15);
		shakeSinA.parametrize(CartDOF.ALL).setDamping(0.7);

		// Lissajous-Schwingung mit Frequenz 1 Hz, Amplitude 50 N, Steifigkeit
		// 1500 [N/m]
		CartesianSineImpedanceControlMode shakeLis;
		shakeLis = CartesianSineImpedanceControlMode.createLissajousPattern(
				CartPlane.XY, 1, 50, 1500);
		shakeLis.parametrize(CartDOF.Z).setStiffness(1000); // ... mit
															// Steifigkeit 1000
															// [N/m] in
															// Z-Richtung
		shakeLis.setRiseTime(3); // ... ueber 3 Sekunden ansteigend

		// Spiral-Schwingung mit Frequenz 15 Hz, Amplitude 16 N, Steifigkeit
		// 1000 [N/m]
		CartesianSineImpedanceControlMode shakeSpirale;
		shakeSpirale = CartesianSineImpedanceControlMode.createSpiralPattern(
				CartPlane.XY, 15, 16, 1000, 180);
		shakeSpirale.setRiseTime(0.2).setHoldTime(60).setFallTime(0.5);

		// Bewegungsprogrammierung
		IMotionContainer handle;
		handle = actTCP.move(ptp(getFrame("/Demo/startP")).setJointVelocityRel(
				0.2));

		while (sel != 4) {
			switch (sel) {
			case 0:
				actSwing = "Sinusschwingung in X-Richtung mit 2 Hz";
				handle = actTCP.moveAsync(positionHold(shakeSinX, -1,
						TimeUnit.SECONDS));
				break;
			case 1:
				actSwing = "Schwingung um Tool Z mit 5 Hz";
				handle = actTCP.moveAsync(positionHold(shakeSinA, -1,
						TimeUnit.SECONDS));
				break;
			case 2:
				actSwing = "Lissajousfigur in XY-Ebene";
				handle = actTCP.moveAsync(positionHold(shakeLis, -1,
						TimeUnit.SECONDS));
				break;
			case 3:
				actSwing = "Spirale in XY-Ebene";
				handle = actTCP.moveAsync(positionHold(shakeSpirale, -1,
						TimeUnit.SECONDS));
				break;
			default:
				break;
			}
			sel = getApplicationUI().displayModalDialog(
					ApplicationDialogType.QUESTION,
					"Auswahl der naechsten Schwingung\n\nAktuell:" + actSwing,
					"Sinus X 2 Hz", "Sinus A 3 Hz", "Lissajous XY 1 Hz",
					"Spirale XY 15 Hz", "ENDE");
			handle.cancel();
			actTCP.move(ptp(getFrame("/Demo/startP")).setJointVelocityRel(0.2));
		}

		actTCP.move(ptp(getFrame("/Demo/startP")).setJointVelocityRel(0.2));
	}

	private void collisionDetection(int runs) {
		getLogger().info("Collision Detection");

		// Bewegungsprogrammierung
		Spline liegendeAcht = new Spline(spl(getFrame("/Demo/startP/P2")),
				spl(getFrame("/Demo/startP/P3")),
				spl(getFrame("/Demo/startP/P1")),
				spl(getFrame("/Demo/startP/P4")),
				spl(getFrame("/Demo/startP/P5")),
				spl(getFrame("/Demo/startP/P1")),
				spl(getFrame("/Demo/startP/P2")),
				spl(getFrame("/Demo/startP/P3")),
				spl(getFrame("/Demo/startP/P1")),
				spl(getFrame("/Demo/startP/P4")),
				spl(getFrame("/Demo/startP/P5")),
				spl(getFrame("/Demo/startP/P1"))).setCartVelocity(150)
				.setCartAcceleration(2000).breakWhen(forceCon);

		IMotionContainer motion;
		for (int i = 0; i < runs; i++) {
			motion = actTCP.move(liegendeAcht);
			if (motion.hasFired(forceCon)) {
				// Reaktion auf Kollision
				boolean resumeMotion = behaviourAfterCollision();
				if (!resumeMotion)
					break;
			}
		}
	}

	private ICondition defineSensitivity() {
		double sensCLS = getApplicationData().getProcessData("sensCLS")
				.getValue();
		getLogger().info(
				"Aktuelle Empfindlichkeit jeder Achse: " + sensCLS
						+ " Nm\nWert über Prozessdaten veränderbar.");

		// Offsetkompensation
		double actTJ1 = lBR_iiwa_7_R800_1.getExternalTorque()
				.getSingleTorqueValue(JointEnum.J1);
		double actTJ2 = lBR_iiwa_7_R800_1.getExternalTorque()
				.getSingleTorqueValue(JointEnum.J2);
		double actTJ3 = lBR_iiwa_7_R800_1.getExternalTorque()
				.getSingleTorqueValue(JointEnum.J3);
		double actTJ4 = lBR_iiwa_7_R800_1.getExternalTorque()
				.getSingleTorqueValue(JointEnum.J4);
		double actTJ5 = lBR_iiwa_7_R800_1.getExternalTorque()
				.getSingleTorqueValue(JointEnum.J5);
		double actTJ6 = lBR_iiwa_7_R800_1.getExternalTorque()
				.getSingleTorqueValue(JointEnum.J6);
		double actTJ7 = lBR_iiwa_7_R800_1.getExternalTorque()
				.getSingleTorqueValue(JointEnum.J7);

		getLogger().info(
				"Offsetwerte\nJ1 " + actTJ1 + "Nm\nJ2 " + actTJ2 + "Nm\nJ3 "
						+ actTJ3 + "Nm\nJ4 " + actTJ4 + "Nm\nJ5 " + actTJ5
						+ "Nm\nJ6 " + actTJ6 + "Nm\nJ7 " + actTJ7 + "Nm");

		// Abbruchbedingungen pro Achse
		JointTorqueCondition jt1 = new JointTorqueCondition(JointEnum.J1,
				-sensCLS + actTJ1, sensCLS + actTJ1);
		JointTorqueCondition jt2 = new JointTorqueCondition(JointEnum.J2,
				-sensCLS + actTJ2, sensCLS + actTJ2);
		JointTorqueCondition jt3 = new JointTorqueCondition(JointEnum.J3,
				-sensCLS + actTJ3, sensCLS + actTJ3);
		JointTorqueCondition jt4 = new JointTorqueCondition(JointEnum.J4,
				-sensCLS + actTJ4, sensCLS + actTJ4);
		JointTorqueCondition jt5 = new JointTorqueCondition(JointEnum.J5,
				-sensCLS + actTJ5, sensCLS + actTJ5);
		JointTorqueCondition jt6 = new JointTorqueCondition(JointEnum.J6,
				-sensCLS + actTJ6, sensCLS + actTJ6);
		JointTorqueCondition jt7 = new JointTorqueCondition(JointEnum.J7,
				-sensCLS + actTJ7, sensCLS + actTJ7);

		ICondition forceCon = jt1.or(jt2, jt3, jt4, jt5, jt6, jt7);
		return forceCon;
	}

	private boolean behaviourAfterCollision() {
		boolean resumeMotion = true;
		int sel = 0;
		IMotionContainer handle;

		handle = actTCP.moveAsync(positionHold(soft, -1, TimeUnit.SECONDS));
		sel = getApplicationUI().displayModalDialog(
				ApplicationDialogType.QUESTION,
				"Kollision ist aufgetreten; LBR ist nachgiebig",
				"Bewegung fortsetzen", "zum Hauptmenü      ");
		handle.cancel();
		if (sel != 0) {
			resumeMotion = false;
			actTCP.move(ptp(getFrame("/Demo/startP")).setJointVelocityRel(.3));
		}

		return resumeMotion;
	}

	private void moveBatch(MotionBatch tempBatch, double relativeVelocity,
			double relativeBlending) {
		Boolean batchFinished = false;
		IMotionContainer myContainer = null;
		IFiredConditionInfo myCondInfo = null;
		IMotion stoppedMotion = null;

		while (!batchFinished) {
			myContainer = actTCP.move(tempBatch.breakWhen(forceCon)
					.setJointVelocityRel(relativeVelocity)
					.setBlendingRel(relativeBlending));
			myCondInfo = myContainer.getFiredBreakConditionInfo();

			if (myCondInfo != null) {
				getLogger().info("collision detected!");
				actTCP.move(positionHold(soft, 1000, TimeUnit.MILLISECONDS));

				stoppedMotion = myCondInfo.getStoppedMotion();
				tempBatch = createShorterBatch(tempBatch, stoppedMotion);
				batchFinished = false;
			} else
				batchFinished = true;

		}
	}

	private MotionBatch createShorterBatch(MotionBatch longBatch,
			IMotion stoppedMotion) {

		List<RobotMotion<?>> reducedBatch_list = new ArrayList<RobotMotion<?>>();
		RobotMotion<?> motion;
		List<RobotMotion<?>> latestBatch = longBatch.getMotions();
		Boolean newStartFound = false;

		ListIterator<RobotMotion<?>> myIterator = latestBatch.listIterator();
		while (myIterator.hasNext()) {
			motion = myIterator.next();
			if (motion.equals(stoppedMotion) && !newStartFound) {
				newStartFound = true;
				reducedBatch_list.add(motion);
			} else if (newStartFound) {
				reducedBatch_list.add(motion);
			}

		}
		MotionBatch reducedBatch = new MotionBatch(
				reducedBatch_list.toArray(new RobotMotion<?>[reducedBatch_list
						.size()]));
		return reducedBatch;
	}

	@Override
	public void run() {
		int sel = -1;
		while (sel != 8) {
			sel = getApplicationUI()
					.displayModalDialog(
							ApplicationDialogType.QUESTION,
							"Was wollen sie tun? (ACHTUNG: iiwa bewegt sich direkt nach Tastendruck!)\n\n(Betrieb nur in T1/T2; Zustimmschalter und Starttaste immer gedrückt halten)",
							"Startposition       ", // 0
							"Kartesische Bewegung", // 1
							"kinematische Redundanz", // 2
							"Nachgiebigkeit      ", // 3
							"Rütteln u. Schütteln", // 4
							"Kollisionserkennung ", // 5
							"Teaching by Demonstration",// 6
							"Teaching mit Handführen", // 7
							"Applikation beenden ");

			switch (sel) {
			case 0:
				lBR_iiwa_7_R800_1.setESMState("2");
				actTCP.move(ptp(getFrame("/Demo/startP")).setJointVelocityRel(
						0.2));
				break;
			case 1:
				lBR_iiwa_7_R800_1.setESMState("2");
				moveCartesian(1);
				break;
			case 2:
				lBR_iiwa_7_R800_1.setESMState("2");
				moveNullspace(1);
				break;
			case 3:
				lBR_iiwa_7_R800_1.setESMState("3");
				stiffness();
				break;
			case 4:
				lBR_iiwa_7_R800_1.setESMState("2");
				wiggleBounce();
				break;
			case 5:
				lBR_iiwa_7_R800_1.setESMState("2");
				collisionDetection(3);
				break;
			case 6:
				lBR_iiwa_7_R800_1.setESMState("2");
				recordTrack();
				break;
			case 7:
				lBR_iiwa_7_R800_1.setESMState("2");
				teachMe();
				break;

			default:
				lBR_iiwa_7_R800_1.setESMState("2");
				actTCP.move(ptp(getFrame("/Demo/startP")).setJointVelocityRel(
						0.2));
				break;
			}
		}
	}
}
