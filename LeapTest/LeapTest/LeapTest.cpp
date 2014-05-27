/******************************************************************************\
* Copyright (C) 2012-2013 Leap Motion, Inc. All rights reserved.               *
* Leap Motion proprietary and confidential. Not for distribution.              *
* Use subject to the terms of the Leap Motion SDK Agreement available at       *
* https://developer.leapmotion.com/sdk_agreement, or another agreement         *
* between Leap Motion and you, your company or other organization.             *
*                                                                              *
* Modified by LI ZHEN, Mar 17th, 2014.                                         *
\******************************************************************************/

#include "stdafx.h"
#include <iostream>
#include <fstream>
#include <sstream>
#include <Windows.h>
#include <ctime>
#include "Leap.h"

using namespace Leap;

std::ofstream fout;//("FingerMove.csv");
std::ofstream tout;//("ToolMove.csv");

class SampleListener : public Listener {
public:
	virtual void onInit(const Controller&);
	virtual void onConnect(const Controller&);
	virtual void onDisconnect(const Controller&);
	virtual void onExit(const Controller&);
	virtual void onFrame(const Controller&);
	virtual void onFocusGained(const Controller&);
	virtual void onFocusLost(const Controller&);
};

void SampleListener::onInit(const Controller& controller) {
	std::cout << "Initialized" << std::endl;
}

void SampleListener::onConnect(const Controller& controller) {
	std::cout << "Connected" << std::endl;
	controller.enableGesture(Gesture::TYPE_CIRCLE);
	controller.enableGesture(Gesture::TYPE_KEY_TAP);
	controller.enableGesture(Gesture::TYPE_SCREEN_TAP);
	controller.enableGesture(Gesture::TYPE_SWIPE);

}

void SampleListener::onDisconnect(const Controller& controller) {
	//Note: not dispatched when running in a debugger.
	std::cout << "Disconnected" << std::endl;
}

void SampleListener::onExit(const Controller& controller) {
	std::cout << "Exited" << std::endl;
}

void SampleListener::onFrame(const Controller& controller) {
	// Get the most recent frame and report some basic information
	const Frame frame = controller.frame();
	std::cout << "Frame id: " << frame.id()
		<< ", timestamp: " << frame.timestamp()
		<< ", hands: " << frame.hands().count()
		<< ", fingers: " << frame.fingers().count()
		<< ", tools: " << frame.tools().count()
		<< ", gestures: " << frame.gestures().count() << std::endl;
	//std::cout << "Fingers:" << frame.fingers().count() << ":";
	if (!frame.hands().isEmpty()) {
		// Get the first hand
		const Hand hand = frame.hands()[0];

		// Check if the hand has any fingers
		const FingerList fingers = hand.fingers();
		float x[10], y[10], z[10];
		if (!fingers.isEmpty()) {
			// Calculate the hand's average finger tip position
			Vector avgPos;

			fout << frame.timestamp() << " , ";
			for (int i = 0; i < fingers.count(); ++i) {
				avgPos += fingers[i].tipPosition();
				x[i] = fingers[i].tipPosition().x;
				y[i] = fingers[i].tipPosition().y;
				z[i] = fingers[i].tipPosition().z;
				fout << i << " , " << x[i] << " , " << y[i] << " , " << z[i] << " , ";
			}
			if (fingers.count() <= 3)
			{
				fout << sqrt(pow(x[0] - x[1], 2) + pow(y[0] - y[1], 2) + pow(z[0]-z[1], 2)) << " , "
					<< sqrt(pow(x[0] - x[2], 2) + pow(y[0] - y[2], 2) + pow(z[0]-z[2], 2)) << " , "
					<< sqrt(pow(x[2] - x[1], 2) + pow(y[2] - y[1], 2) + pow(z[2]-z[1], 2)) << " , ";
			}
			fout << std::endl;

			avgPos /= (float)fingers.count();
			std::cout << "Hand has " << fingers.count()
				<< " fingers, average finger tip position" << avgPos << std::endl;
		}

		// Get the hand's sphere radius and palm position
		//std::cout << "Hand sphere radius: " << hand.sphereRadius()
		//	<< " mm, palm position: " << hand.palmPosition() << std::endl;

		// Get the hand's normal vector and direction
		// const Vector normal = hand.palmNormal();
		// const Vector direction = hand.direction();

		// Calculate the hand's pitch, roll, and yaw angles
		/*std::cout << "Hand pitch: " << direction.pitch() * RAD_TO_DEG << " degrees, "
			<< "roll: " << normal.roll() * RAD_TO_DEG << " degrees, "
			<< "yaw: " << direction.yaw() * RAD_TO_DEG << " degrees" << std::endl;*/
	}

	if (!frame.tools().isEmpty())
	{
		const ToolList tools = frame.tools();
		tout << frame.timestamp() << " , ";
		for (int i = 0; i < tools.count(); ++i)
		{
			tout << i << " , " << tools[i].tipPosition().x << " , " << tools[i].tipPosition().y << " , " << 
				tools[i].tipPosition().z << " , ";
			//std::cout << i << " , " << tools[i].tipPosition().x << " , " << tools[i].tipPosition().y << " , " << 
			//	tools[i].tipPosition().z << " , " << frame.timestamp() << std::endl;
		}

		tout << std::endl;
	}
	//// Get gestures
	//const GestureList gestures = frame.gestures();
	//for (int g = 0; g < gestures.count(); ++g) {
	//	Gesture gesture = gestures[g];

	//	switch (gesture.type()) {
	//	case Gesture::TYPE_CIRCLE:
	//		{
	//			CircleGesture circle = gesture;
	//			std::string clockwiseness;

	//			if (circle.pointable().direction().angleTo(circle.normal()) <= PI/4) {
	//				clockwiseness = "clockwise";
	//			} else {
	//				clockwiseness = "counterclockwise";
	//			}

	//			// Calculate angle swept since last frame
	//			float sweptAngle = 0;
	//			if (circle.state() != Gesture::STATE_START) {
	//				CircleGesture previousUpdate = CircleGesture(controller.frame(1).gesture(circle.id()));
	//				sweptAngle = (circle.progress() - previousUpdate.progress()) * 2 * PI;
	//			}
	//			std::cout << "Circle id: " << gesture.id()
	//				<< ", state: " << gesture.state()
	//				<< ", progress: " << circle.progress()
	//				<< ", radius: " << circle.radius()
	//				<< ", angle " << sweptAngle * RAD_TO_DEG
	//				<<  ", " << clockwiseness << std::endl;
	//			break;
	//		}
	//	case Gesture::TYPE_SWIPE:
	//		{
	//			SwipeGesture swipe = gesture;
	//			std::cout << "Swipe id: " << gesture.id()
	//				<< ", state: " << gesture.state()
	//				<< ", direction: " << swipe.direction()
	//				<< ", speed: " << swipe.speed() << std::endl;
	//			break;
	//		}
	//	case Gesture::TYPE_KEY_TAP:
	//		{
	//			KeyTapGesture tap = gesture;
	//			std::cout << "Key Tap id: " << gesture.id()
	//				<< ", state: " << gesture.state()
	//				<< ", position: " << tap.position()
	//				<< ", direction: " << tap.direction()<< std::endl;
	//			break;
	//		}
	//	case Gesture::TYPE_SCREEN_TAP:
	//		{
	//			ScreenTapGesture screentap = gesture;
	//			std::cout << "Screen Tap id: " << gesture.id()
	//				<< ", state: " << gesture.state()
	//				<< ", position: " << screentap.position()
	//				<< ", direction: " << screentap.direction()<< std::endl;
	//			break;
	//		}
	//	default:
	//		std::cout << "Unknown gesture type." << std::endl;
	//		break;
	//	}
	//}

	//if (!frame.hands().isEmpty() || !gestures.isEmpty()) {
	//	std::cout << std::endl;
	//}
}

void SampleListener::onFocusGained(const Controller& controller) {
	std::cout << "Focus Gained" << std::endl;
}

void SampleListener::onFocusLost(const Controller& controller) {
	std::cout << "Focus Lost" << std::endl;
}

int main() {

	int num(10);

	std::cout << "Num: " << num << std::endl;

	std::stringstream strF, strT;
	strF << "FingerMove2" << num << ".csv";
	fout.open(strF.str());
	
	strT << "ToolMove2" << num << ".csv";
	tout.open(strT.str());

	// Create a sample listener and controller
	SampleListener listener;
	Controller controller;

	// Count from 00:00:00, Jan 1st, 1970.
	SYSTEMTIME sys;
	time_t timer;
	time(&timer);
	GetLocalTime(&sys);
	fout << "time since 1970(s), ms" << std::endl;
	tout << "time since 1970(s), ms" << std::endl;
	fout << timer << ", " << sys.wMilliseconds << std::endl;
	tout << timer << ", " << sys.wMilliseconds << std::endl;

	fout << "timestamp(us), fingerID, X1, Y1, Z1, fingerID, X2, Y2, Z2, fingerID, X3, Y3, Z3, edge1, edge2, edge3" << std::endl;
	tout << "timestamp(us), toolID,   X1, Y1, Z1, toolID,   X2, Y2, Z2, toolID,   X3, Y3, Z3" << std::endl;
	// Have the sample listener receive events from the controller
	controller.addListener(listener);

	// Keep this process running until Enter is pressed
	std::cout << "Press Enter to quit..." << std::endl;
	std::cin.get();

	// Remove the sample listener when done
	controller.removeListener(listener);
	fout.close();
	tout.close();

	return 0;
}

