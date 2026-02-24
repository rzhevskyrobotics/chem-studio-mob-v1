package com.example.smart_labs.mqtt

object MqttTopics {
    const val OUT_TEMP = "spbOfficeRR/LyE1JOQF/status"                 // улица
    const val LAB_TEMP = "spbOfficeRR/Simulation25/lab_temp"          // лаборатория
    const val REACT_TEMP = "spbOfficeRR/Simulation25/react_temp"      // реактор температура
    const val REACT_PRESS = "spbOfficeRR/Simulation25/react_press"    // реактор давление

    const val DISP_1_POW = "spbOfficeRR/Simulation25/disp_1_pow"
    const val DISP_1_SPEED = "spbOfficeRR/Simulation25/disp_1_speed"

    const val DISP_2_POW = "spbOfficeRR/Simulation25/disp_2_pow"
    const val DISP_2_SPEED = "spbOfficeRR/Simulation25/disp_2_speed"

    const val REACT_TEMP_SET = "spbOfficeRR/Simulation25/react_temp_set"
    const val REACT_TEMP_TARGET = "spbOfficeRR/Simulation25/react_temp_target"

    val ALL_SUBSCRIBE = listOf(
        OUT_TEMP, LAB_TEMP, REACT_TEMP, REACT_PRESS,
        DISP_1_POW, DISP_1_SPEED,
        DISP_2_POW, DISP_2_SPEED,
        REACT_TEMP_SET, REACT_TEMP_TARGET
    )
}
