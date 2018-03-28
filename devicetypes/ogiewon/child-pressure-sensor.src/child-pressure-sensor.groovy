/**
 *  Child Temperature Sensor
 *
 *  Copyright 2017 Daniel Ogorchock
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Change History:
 *
 *    Date        Who            What
 *    ----        ---            ----
 *    2017-04-10  Dan Ogorchock  Original Creation
 *    2017-08-23  Allan (vseven) Added a generateEvent routine that gets info from the parent device.  This routine runs each time the value is updated which can lead to other modifications of the device.
 *    2017-11-04  Dan Ogorchock  Added preference for Temperature Unit Conversion: Fahrenheit to Celsius, Celsius to Fahrenheit, or none 
 *    2018-02-16  Dan OGorchock  Fixed preferences to work with Hubitat.
 *    2018-03-28  Josh Hill      Modified for pressure sensor
 * 
 */
metadata {
	definition (name: "Child Pressure Sensor", namespace: "ogiewon", author: "Daniel Ogorchock") {
		capability "Sensor"

		attribute "lastUpdated", "String"
		attribute "humidity", "String"

		command "generateEvent", ["string", "string"]
	}

	simulator {

	}
    
	preferences {
		section("Prefs") {
			//input title: "Temperature Offset", description: "This feature allows you to correct any temperature variations by selecting an offset. Ex: If your sensor consistently reports a temp that's 5 degrees too warm, you'd enter \"-5\". If 3 degrees too cold, enter \"+3\".", displayDuringSetup: false, type: "paragraph", element: "paragraph"
			input "pressureOffset", "number", title: "Pressure Offset", description: "Adjust pressure by this much", range: "*..*", displayDuringSetup: false
			//input title: "Temperature Unit Conversion", description: "This feature allows you to select F to C, C to F, or no conversion", displayDuringSetup: false, type: "paragraph", element: "paragraph"
			input "pressureUnitConversion", "enum", title: "Pressure Unit Conversion - select hPa to atm, hPa to Hg, or no conversion", description: "", defaultValue: "1", required: true, multiple: false, options:[["1":"none"], ["2":"hPa to atm"], ["3":"hPa to Hg"]], displayDuringSetup: false
		}
	}
    
	tiles(scale: 2) {
		multiAttributeTile(name: "pressure", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.pressure", key: "PRIMARY_CONTROL") {
				attributeState("pressure", label: '${currentValue}°', unit:"hPa", defaultState: true, 
						backgroundColors: [
                                // Celsius
                                [value: 0, color: "#153591"],
                                [value: 7, color: "#1e9cbb"],
                                [value: 15, color: "#90d2a7"],
                                [value: 23, color: "#44b621"],
                                [value: 28, color: "#f1d801"],
                                [value: 35, color: "#d04e00"],
                                [value: 37, color: "#bc2323"],
                                // Fahrenheit
                                [value: 40, color: "#153591"],
                                [value: 44, color: "#1e9cbb"],
                                [value: 59, color: "#90d2a7"],
                                [value: 74, color: "#44b621"],
                                [value: 84, color: "#f1d801"],
                                [value: 95, color: "#d04e00"],
                                [value: 96, color: "#bc2323"]
						])
			}
 			tileAttribute("device.lastUpdated", key: "SECONDARY_CONTROL") {
    				attributeState("default", label:'    Last updated ${currentValue}',icon: "st.Health & Wellness.health9")
            }
		}
	}
}

def generateEvent(String name, String value) {
	//log.debug("Passed values to routine generateEvent in device named $device: Name - $name  -  Value - $value")
	// Offset the temperature based on preference
    def offsetValue = Math.round((Float.parseFloat(value))*100.0)/100.0d
    if (pressureOffset) {
    	offsetValue = offsetValue + pressureOffset
    }
    
    if (pressureUnitConversion == "2") {
    	//log.debug "pressureUnitConversion = ${pressureUnitConversion}"
        double pressure_atm = offsetValue.toFloat()*0.00098692326671601  //convert from hPa to atm
        offsetValue = pressure_atm.round(2)
	}
    
    if (tempUnitConversion == "3") {
    	//log.debug "tempUnitConversion = ${tempUnitConversion}"
        double pressureHg = offsetValue.toFloat()*100/3386.39  //convert hPa to Hg
        offsetValue = pressureHg.round(2)
	}
    
    // Update device
	sendEvent(name: name, value: (String)offsetValue)
    // Update lastUpdated date and time
    def nowDay = new Date().format("MMM dd", location.timeZone)
    def nowTime = new Date().format("h:mm a", location.timeZone)
    sendEvent(name: "lastUpdated", value: nowDay + " at " + nowTime, displayed: false)
}


def installed() {
}
