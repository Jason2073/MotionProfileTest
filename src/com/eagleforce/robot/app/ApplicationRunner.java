package com.eagleforce.robot.app;

import java.util.List;

import com.eagleforce.robot.model.MotionProfileConfiguration;
import com.eagleforce.robot.model.MotionProfilePoint;
import com.eagleforce.robot.service.MotionProfileService;

public class ApplicationRunner {

	MotionProfileService mpService = new MotionProfileService();

	public void run() {
		// TODO: Extract to properties or something
		MotionProfileConfiguration mpc = new MotionProfileConfiguration();
		mpc.setMaxVel(3);
		mpc.setEndDistance(1);
		mpc.setInterval(10);
		mpc.setMaxAcc(15);
		
		List<MotionProfilePoint> mppList = mpService.generatePoints(mpc);
		
		for (MotionProfilePoint motionProfilePoint : mppList) {
			System.out.println("Print out points here");
//			System.out.println(i + "\t" + velOfI + "\t" + pos + "\t" + acc + "\t" + truncateDecimal(pos, 2) + "\t"
//					+ truncateDecimal(endDistance, 2));

		}
	}

}
