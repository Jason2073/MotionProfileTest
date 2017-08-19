package com.eagleforce.robot.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.eagleforce.robot.model.MotionProfileConfiguration;
import com.eagleforce.robot.model.MotionProfilePoint;

public class MotionProfileService {

	private static final String ROBOT_MAX_VELOCITY_KEY = "robot.specs.max-vel";
	private static final String ROBOT_MAX_ACCELERATION_KEY = "robot.specs.max-acc";
	private static final String MOTION_DISTANCE_KEY = "motion-profiling.distance";
	private static final String MOTION_INTERVAL_KEY = "motion-profiling.interval";
	// private static final String MOTION_T1_KEY = "motion-profiling.t1";
	// private static final String MOTION_T2_KEY = "motion-profiling.t2";
	
	private static final int RENAME_THIS = 3000;

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

	public List<MotionProfilePoint> generatePoints(MotionProfileConfiguration mpc) {
		List<MotionProfilePoint> mppList = new ArrayList<>();
		
		// Store config in easy to access variables
		final double maxVel = mpc.getMaxVel();
		final double endDistance = mpc.getEndDistance();
		final double interval = mpc.getInterval();
		final double maxAcc = mpc.getMaxAcc();

		// Resolve non-config, static variables
//		final double t1 = (1. / (maxAcc / RENAME_THIS));
		final double t1 = RENAME_THIS / maxAcc;
		final double t2 = t1 / 2;
		double f2;
		final List<Double> f1List = new ArrayList<>();
		
		// Initialize everything to zero for the first record
		mppList.add(initialMpp(interval));
		f1List.add(0.0);
		f2 = 0;
		
		// Create a counter to use while looping
		int i = 0;
		
		while (true) {
			i++;
			double posOrNeg;
			MotionProfilePoint mpp = new MotionProfilePoint();
			// TODO: Decide which version below is better/more readable
//			MotionProfilePoint prevMpp = mppList.listIterator().previous();
			MotionProfilePoint prevMpp = mppList.get(i - 1);
			
			posOrNeg = renameThisMethod(i, endDistance, maxVel, interval, t1);
			double renameThis = Math.max(0, Math.min(1, (f1List.get(i - 1) + posOrNeg))); 
			f1List.add(renameThis);
			f2 = calculateF2(t2, i, interval, f1List);
			
			mpp.setInterval(interval);
			mpp.setVel(calculateVelocity(maxVel, f1List, f2, i, t2, interval));
			mpp.setPos(prevMpp.getPos() + calculatePosition(mpp, prevMpp, interval));
			// TODO: Decide whether we should keep or remove acceleration
			
			mppList.add(mpp);
			
			if(endDistanceReached(mpp.getPos(), endDistance))
				break;
		}
		
		return mppList;
	}
	
	// Private helper methods
	// ====================================================================================================
	private MotionProfilePoint initialMpp(double interval) {
		MotionProfilePoint mpp = new MotionProfilePoint();
		mpp.setAcc(0);
		mpp.setInterval(interval);
		mpp.setPos(0);
		mpp.setVel(0);
		
		return mpp;
	}
	
	// TODO: Break these variables out into a model object to be passed around
	private double renameThisMethod(int i, double endDistance, double maxVel, double interval, double t1) {
		if (i - 1 < (((endDistance / maxVel) * 1000) / interval)) {
			return (1. / Math.round(t1 / interval));
		} else {
			return ((-1.) / Math.round(t1 / interval));
		}
	}
	
	private double calculateF2(double t2, int i, double interval, final List<Double> f1List) {
		double f2 = 0;
		int sum = (int) (Math.round(t2 / interval));
		if (i == (int) Math.min(Math.round(t2 / interval), i)) {
			f2 += f1List.get(i);
		} else {
			f2 = 0;
			for (int j = 0; j < sum; j++) {
				f2 += f1List.get(i - j);
			}
		}
		
		return f2;
	}

	private double calculateVelocity(final double maxVel, final List<Double> f1List, final double f2, int i, double t2, double interval) {
		return maxVel * ((f1List.get(i) + f2) / (1 + (Math.round(t2 / interval))));
	}
	
	private double calculatePosition(MotionProfilePoint currMpp, MotionProfilePoint prevMpp, double interval) {
		final double avgVel = (currMpp.getVel() + prevMpp.getVel()) / 2; 
		return ((avgVel * interval) / 1000);
	}
	
	private boolean endDistanceReached(final double pos, final double endDistance) {
		return truncateDecimal(pos, 3).equals(truncateDecimal(endDistance, 3));
	}
	
	private BigDecimal truncateDecimal(double x, int numberofDecimals) {
		if (x > 0) {
			return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_FLOOR);
		} else {
			return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_CEILING);
		}
	}
	
	
}
