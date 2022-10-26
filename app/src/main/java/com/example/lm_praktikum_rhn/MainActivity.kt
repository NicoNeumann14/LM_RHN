package com.example.lm_praktikum_rhn

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import org.w3c.dom.Text
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    //Buttons
    private lateinit var btnStart: Button
    private lateinit var btnStop:Button

    //RadioGroup
    private lateinit var rgSamplingSensor:RadioGroup

    //View id´s
    private var idBeschleunigung:ArrayList<Int> = arrayListOf(R.id.tvBeschX,R.id.tvBeschY,R.id.tvBeschZ,R.id.tvBeschMag)
    private var idGyroskop:ArrayList<Int> = arrayListOf(R.id.tvGyroX,R.id.tvGyroY,R.id.tvGyroZ)
    private var idMagnometer:ArrayList<Int> = arrayListOf(R.id.tvMagneX,R.id.tvMagneY,R.id.tvMagneZ)

    //TextViews
    private var tvBeschleunigung:ArrayList<TextView> = ArrayList()
    private var tvGyroskop:ArrayList<TextView> = ArrayList()
    private var tvMagnometer:ArrayList<TextView> = ArrayList()
    private lateinit var tvLicht:TextView
    private lateinit var tvDruck:TextView
    private lateinit var tvProxi:TextView

    //Sensoren
    private lateinit var sensorManager:SensorManager
    private lateinit var sensorBeschleunigung: Sensor
    private lateinit var sensorGyroskop: Sensor
    private lateinit var sensorMagnometer: Sensor
    private lateinit var sensorLicht:Sensor
    private lateinit var sensorDruck:Sensor
    private lateinit var sensorProxi:Sensor

    //Sensor Samplings
    private lateinit var samplings:ArrayList<Int>

    //SensorData
    private var beschleunigungsData:SensorData? = null
    private var gyroskopData:SensorData? = null
    private var magnometerData:SensorData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        initSensoren()
    }

    private fun initViews(){
        //TextViews
        for(i in idBeschleunigung){
            tvBeschleunigung.add(findViewById(i))
        }
        for(i in idGyroskop){
            tvGyroskop.add(findViewById(i))
        }
        for(i in idMagnometer){
            tvMagnometer.add(findViewById(i))
        }
        tvLicht = findViewById(R.id.tvLichtRes)
        tvDruck = findViewById(R.id.tvDruckRes)
        tvProxi = findViewById(R.id.tvProxiRes)

        //RadioGroup
        rgSamplingSensor = findViewById(R.id.rgSamplinsSensor)

        //Buttons
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)

        //Button Listener
        btnStart.setOnClickListener{
            registerListener()
            btnStart.isEnabled = false
            btnStop.isEnabled = true
        }

        btnStop.setOnClickListener {
            unregisterListener()
            btnStart.isEnabled = true
            btnStop.isEnabled = false
        }
    }
    private fun initSensoren(){
        //Manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        //Samplings in Array 0-3
        samplings = arrayListOf(SensorManager.SENSOR_DELAY_NORMAL,SensorManager.SENSOR_DELAY_UI,SensorManager.SENSOR_DELAY_GAME,SensorManager.SENSOR_DELAY_FASTEST)

        //Sensoren
        if(sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null){
            sensorBeschleunigung = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        }

        if(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            sensorGyroskop = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        }

        if(sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null){
            sensorMagnometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        }

        if(sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null){
            sensorLicht = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        }

        if(sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null){
            sensorDruck = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        }

        if(sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null){
            sensorProxi = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        }
    }

    private fun registerListener(){

        //RadioGroup, RadioButton Check and set index
        var index: Int = 0
        var id:Int = rgSamplingSensor.checkedRadioButtonId
        val radio:RadioButton = findViewById(id)
        if (radio.text.equals("Ui")){
            index = 1
        }else if(radio.text.equals("Game")){
            index = 2
        }else if(radio.text.equals("Fastest")){
            index = 3
        }

        //todo Berechtigung abfragen wenn der User sich für SamplinRate: Fastest entscheidet
        if(index == 3){

        }

        if(sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null){
           sensorManager.registerListener(this,sensorBeschleunigung,samplings[index])
        }

        if(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            sensorManager.registerListener(this,sensorGyroskop,samplings[index])
        }

        if(sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null){
            sensorManager.registerListener(this,sensorMagnometer,samplings[index])
        }

        if(sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null){
            sensorManager.registerListener(this,sensorLicht,samplings[index])
        }

        if(sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null){
            sensorManager.registerListener(this,sensorDruck,samplings[index])
        }

        if(sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null){
            sensorManager.registerListener(this,sensorProxi,samplings[index])
        }
    }
    private fun unregisterListener(){
        sensorManager.unregisterListener(this,sensorBeschleunigung)
        sensorManager.unregisterListener(this,sensorGyroskop)
        sensorManager.unregisterListener(this,sensorMagnometer)
        sensorManager.unregisterListener(this,sensorLicht)
        sensorManager.unregisterListener(this,sensorDruck)
        sensorManager.unregisterListener(this,sensorProxi)
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        if(p0!!.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION){
            getBeschleunigungsData(p0)
        }else if(p0.sensor.type == Sensor.TYPE_GYROSCOPE){
            getGyroskopData(p0)
        }else if(p0.sensor.type == Sensor.TYPE_MAGNETIC_FIELD){
            getMagnetoData(p0)
        }else if(p0.sensor.type == Sensor.TYPE_LIGHT){
            tvLicht.text = "${p0!!.values[0]}"
        }else if(p0.sensor.type == Sensor.TYPE_PRESSURE){
            tvDruck.text = "${"%.2f".format(p0!!.values[0])}"
        }else if(p0.sensor.type == Sensor.TYPE_PROXIMITY){
            tvProxi.text = "${p0!!.values[0]}"
        }
    }
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //notUsedNow
    }

    private fun getBeschleunigungsData(p0: SensorEvent?){
        if(beschleunigungsData == null){
            beschleunigungsData = SensorData(p0!!.values[0],p0!!.values[1],p0!!.values[2],p0!!.timestamp)
        }else{
            beschleunigungsData!!.x = p0!!.values[0]
            beschleunigungsData!!.y = p0!!.values[1]
            beschleunigungsData!!.z = p0!!.values[2]
            beschleunigungsData!!.timestamp = p0!!.timestamp
        }

        tvBeschleunigung[0].text = "${"%.2f".format(beschleunigungsData!!.x)}"
        tvBeschleunigung[1].text = "${"%.2f".format(beschleunigungsData!!.y)}"
        tvBeschleunigung[2].text = "${"%.2f".format(beschleunigungsData!!.z)}"
        tvBeschleunigung[3].text = "${"%.2f".format(sqrt((Math.pow(beschleunigungsData!!.x.toDouble(),2.0)+Math.pow(
            beschleunigungsData!!.y.toDouble(),2.0)+Math.pow(beschleunigungsData!!.z.toDouble(),2.0))))}"

    }
    private fun getGyroskopData(p0: SensorEvent?){
        if(gyroskopData == null){
            gyroskopData = SensorData(p0!!.values[0],p0!!.values[1],p0!!.values[2],p0!!.timestamp)
        }else{
            gyroskopData!!.x = p0!!.values[0]
            gyroskopData!!.y = p0!!.values[1]
            gyroskopData!!.z = p0!!.values[2]
            gyroskopData!!.timestamp = p0!!.timestamp
        }

        tvGyroskop[0].text = "${"%.2f".format(gyroskopData!!.x)}"
        tvGyroskop[1].text = "${"%.2f".format(gyroskopData!!.y)}"
        tvGyroskop[2].text = "${"%.2f".format(gyroskopData!!.z)}"
    }
    private fun getMagnetoData(p0: SensorEvent?){
        if(magnometerData == null){
            magnometerData = SensorData(p0!!.values[0],p0!!.values[1],p0!!.values[2],p0!!.timestamp)
        }else{
            magnometerData!!.x = p0!!.values[0]
            magnometerData!!.y = p0!!.values[1]
            magnometerData!!.z = p0!!.values[2]
            magnometerData!!.timestamp = p0!!.timestamp
        }

        tvMagnometer[0].text = "${"%.2f".format(magnometerData!!.x)}"
        tvMagnometer[1].text = "${"%.2f".format(magnometerData!!.y)}"
        tvMagnometer[2].text = "${"%.2f".format(magnometerData!!.z)}"
    }

}