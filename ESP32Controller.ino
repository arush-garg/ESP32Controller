#include <WiFi.h>
#include <ESP32Servo.h>

const char* ssid = "iPhone (2)";
const char* password = "handGrenade";

const int SLOW_SPEED_REDUCTION = 250;
const int MED_SPEED_REDUCTION = 100;
const int FAST_SPEED_REDUCTION = 0;

WiFiServer server(80);

int rightMotorPin = 2;
int leftMotorPin = 12;

/**Must be a value between 0 and 475*/
int speedReduction = MED_SPEED_REDUCTION;

Servo rightMotor;
Servo leftMotor;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
      delay(100);
      Serial.print(".");
  }
  Serial.print("\nConnected to WiFi: ");
  Serial.println(WiFi.localIP());
  server.begin();

  leftMotor.attach(leftMotorPin);
  rightMotor.attach(rightMotorPin);
}

void loop() {
   WiFiClient client = server.available();
    if (client) {
        String request = client.readStringUntil('\r');
        client.flush();
        
        if (request.indexOf("/W") != -1) {
            moveForward();
        } 
        else if (request.indexOf("/S") != -1) {
            moveBackward();
        } 
        else if (request.indexOf("/A") != -1) {
            turnLeft();
        } 
        else if (request.indexOf("/D") != -1) {
            turnRight();
        }
        else if(request.indexOf("/E") != -1) {
          int mode = request.substring(request.indexOf("/E") + 3, request.indexOf("/E") + 4).toInt();
          Serial.print("Speed mode changed to: ");
          Serial.println(request.substring(request.indexOf("/E") + 3, request.indexOf("/E") + 4));
          if(mode == 1) {
            speedReduction = SLOW_SPEED_REDUCTION;
          }
          else if(mode == 2) {
            speedReduction = MED_SPEED_REDUCTION;
          }
          else {
            speedReduction = FAST_SPEED_REDUCTION;
          }
        }

        else {
            stop();
        }
        
        client.println("HTTP/1.1 200 OK");
        client.println("Content-Type: text/plain");
        client.println("Connection: close");
        client.println();
        client.println("Command received");
        client.stop();
    }
}

void moveForward() {
  Serial.println("Moving forward");
  rightMotor.writeMicroseconds(2000-speedReduction);
  leftMotor.writeMicroseconds(1000+speedReduction);
}

void moveBackward() {
  Serial.println("Moving backward");
  rightMotor.writeMicroseconds(1000+speedReduction);
  leftMotor.writeMicroseconds(2000-speedReduction);
}

void turnLeft() {
  Serial.println("Turning left");
  rightMotor.writeMicroseconds(1000+speedReduction);
  leftMotor.writeMicroseconds(1000+speedReduction);
}

void turnRight() {
  Serial.println("Turning right");
  rightMotor.writeMicroseconds(2000-speedReduction);
  leftMotor.writeMicroseconds(2000-speedReduction);
}

void stop() {
  Serial.println("Stopped");
  rightMotor.writeMicroseconds(1500);
  leftMotor.writeMicroseconds(1500);
}
