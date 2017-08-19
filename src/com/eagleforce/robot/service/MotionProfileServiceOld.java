package com.eagleforce.robot.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.eagleforce.robot.model.MotionProfileConfiguration;
import com.eagleforce.robot.model.MotionProfilePoint;

public class MotionProfileServiceOld {

	private static final String ROBOT_MAX_VELOCITY_KEY = "robot.specs.max-vel";
	private static final String ROBOT_MAX_ACCELERATION_KEY = "robot.specs.max-acc";
	private static final String MOTION_DISTANCE_KEY = "motion-profiling.distance";
	private static final String MOTION_INTERVAL_KEY = "motion-profiling.interval";
	// private static final String MOTION_T1_KEY = "motion-profiling.t1";
	// private static final String MOTION_T2_KEY = "motion-profiling.t2";

	private Properties motionProfileProps = null;

	public Properties getMotionProfileProps() {
		if (motionProfileProps == null) {
			motionProfileProps = new Properties();
			motionProfileProps.setProperty(ROBOT_MAX_ACCELERATION_KEY, "0");
			motionProfileProps.setProperty(ROBOT_MAX_VELOCITY_KEY, "3");
			// motionProfileProps.setProperty(MOTION_T1_KEY, "400");
			// motionProfileProps.setProperty(MOTION_T2_KEY, "200");
			motionProfileProps.setProperty(MOTION_DISTANCE_KEY, "1");
			motionProfileProps.setProperty(MOTION_INTERVAL_KEY, "10");

		}
		return motionProfileProps;
	}

	public void setMotionProfileProps(Properties motionProfileProps) {
		this.motionProfileProps = motionProfileProps;
	}

	/*
	 * public static void main(String[] args) { velcalc(); MotionProfileService app = new
	 * MotionProfileService(); Properties props = app.getMotionProfileProps(); String maxAcc =
	 * props.getProperty(ROBOT_MAX_ACCELERATION_KEY);
	 * System.out.println(maxAcc);
	 * 
	 * int maxAccInt = Integer.parseInt(maxAcc); System.out.println(maxAccInt);
	 * }
	 */
	public static BigDecimal truncateDecimal(double x, int numberofDecimals) {
		if (x > 0) {
			return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_FLOOR);
		} else {
			return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_CEILING);
		}
	}

	public List<MotionProfilePoint> generatePoints(MotionProfileConfiguration mpc) {
		List<MotionProfilePoint> mppList = new ArrayList<>();
		// static
		final double max_vel = mpc.getMaxVel();
		final double endDistance = mpc.getEndDistance();
		final double interval = mpc.getInterval();
		final double max_acc = mpc.getMaxAcc();
		int numPoints = 1000; // Math.round( time / interval);
		// int maxSize = 10000;

		final double t1 = (1. / (max_acc / 3000));
		final double t2 = t1 / 2;
		
		// dynamic
		double[] f1 = new double[numPoints];
		double f2 = 0;
		
		double pos = 0;
		// double vel = 0;
		double acc;
		double[] velArray = new double[numPoints];

		for (int i = 0; i < numPoints; i++) {

			if (i == 0) {
				// set first record to all 0s
				f1[i] = 0;
				velArray[i] = 0;
				f2 = 0;
				pos = 0;

			} else {
				// f1[0]= 0; //(start_vel / max_vel)*
				double posOrNeg;
				int input;
				
				if (i - 1 < (((endDistance / max_vel) * 1000) / interval)) {
					input = 1;
					// System.out.println("Here");
				} else {
					input = 0;
					// System.out.println("There");
				}
				// System.out.println(input);
				if (input == 1) {
					posOrNeg = (1. / Math.round(t1 / interval));
				} else {
					posOrNeg = ((-1.) / Math.round(t1 / interval));
				}
				// System.out.println(posOrNeg);
				f1[i] = Math.max(0, Math.min(1, (f1[i - 1]) + posOrNeg));
				// f1[i] = posOrNeg;
				// System.out.println(f1[i]);
				// for (int j = 0; j < numPoints; j++) {
				int sum = (int) (Math.round(t2 / interval));
				if (i == (int) Math.min(Math.round(t2 / interval), i)) {
					f2 += f1[i];
				} else {
					f2 = 0;
					for (int j = 0; j < sum; j++) {
						f2 += f1[i - j];
					}
				}

				velArray[i] = (max_vel * ((f1[i] + f2) / (1 + (Math.round(t2 / interval)))));
				double velOfI = velArray[i];
				// System.out.println("velocity is: " + );
				// if (i == 1) {
				pos += ((((velArray[i] + velArray[i - 1]) / 2) * interval) / 1000.);
				// } else {
				// pos += ((((velArray[i - 1] + velArray[i - 2]) / 2) *
				// interval) / 1000.);
				acc = ((velArray[i] - velArray[i - 1]) / (interval / 1000.));
				System.out.println(i + "\t" + velOfI + "\t" + pos + "\t" + acc + "\t" + truncateDecimal(pos, 2) + "\t"
						+ truncateDecimal(endDistance, 2));
			}

//			BigDecimal a = truncateDecimal(pos, 2);
//			BigDecimal b = truncateDecimal(endDistance, 2);
			
			
//			
//			if(a == b) {
//				System.out.println("winning");
//			}
			
			if (truncateDecimal(pos, 3).equals(truncateDecimal(endDistance,3)))/* ((pos-.05) < distance << (pos+.05))) */ {
				break;
				// truncateDecimal(9.62, 2);
			}

		}
		return mppList;
	}

}
