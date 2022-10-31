package com.example.lm_praktikum_rhn

import android.R.attr.path
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Environment
import android.os.NetworkOnMainThreadException
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.jar.Manifest
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(), SensorEventListener, LocationListener {

    //Buttons
    private lateinit var btnStart: Button
    private lateinit var btnStop:Button

    //RadioGroup
    private lateinit var rgSamplingSensor:RadioGroup
    private lateinit var rgLocation:RadioGroup

    //RadioButtons
    private lateinit var rbNetwork:RadioButton
    private lateinit var rbGPS:RadioButton

    //LocationManager
    private lateinit var locationManager:LocationManager

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
    private lateinit var tvLati:TextView
    private lateinit var tvAlti:TextView
    private lateinit var tvLongi:TextView
    private lateinit var tvAccu:TextView



    //Checkboxen
    private lateinit var cbAcce:CheckBox
    private lateinit var cbGyro:CheckBox
    private lateinit var cbMagno:CheckBox
    private lateinit var cbLicht:CheckBox
    private lateinit var cbDruck:CheckBox
    private lateinit var cbProxi:CheckBox

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

    //JSONArray
    private var jsonArray = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        initSensoren()
        initLocation()
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
        tvLongi = findViewById(R.id.tvLongiR)
        tvAlti = findViewById(R.id.tvAltiR)
        tvLati = findViewById(R.id.tvLatiR)
        tvAccu = findViewById(R.id.tvAccuracyR)

        //RadioGroup
        rgSamplingSensor = findViewById(R.id.rgSamplinsSensor)
        rgLocation = findViewById(R.id.rgPosition)

        //RadioButtons
        rbGPS = findViewById(R.id.rbGPS)
        rbNetwork = findViewById(R.id.rbNetwork)

        //Checkboxen
        cbAcce = findViewById(R.id.cbBesch)
        cbGyro = findViewById(R.id.cbGyro)
        cbMagno = findViewById(R.id.cbMagneto)
        cbLicht = findViewById(R.id.cbLicht)
        cbDruck = findViewById(R.id.cbDruck)
        cbProxi = findViewById(R.id.cbProxi)

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
            schreibinTxt()
            btnStart.isEnabled = true
            btnStop.isEnabled = false
        }
    }

    private fun initLocation(){
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }


    private fun schreibinTxt(){
        var fileName = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS)
        var fileS = File(fileName,"test.txt")

        fileS.printWriter().use { out->

            for(i in 0 until jsonArray.length()){

                out.println(jsonArray[i].toString())
            }

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


        if(sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null && cbAcce.isChecked){
           sensorManager.registerListener(this,sensorBeschleunigung,samplings[index])
        }

        if(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null && cbGyro.isChecked){
            sensorManager.registerListener(this,sensorGyroskop,samplings[index])
        }

        if(sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null && cbMagno.isChecked){
            sensorManager.registerListener(this,sensorMagnometer,samplings[index])
        }

        if(sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null && cbLicht.isChecked){
            sensorManager.registerListener(this,sensorLicht,samplings[index])
        }

        if(sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null && cbDruck.isChecked){
            sensorManager.registerListener(this,sensorDruck,samplings[index])
        }

        if(sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null && cbProxi.isChecked){
            sensorManager.registerListener(this,sensorProxi,samplings[index])
        }

        //Location ab hier

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 100)
        }
        val selectedRadioButoon:Int = rgLocation.checkedRadioButtonId
        if(rbNetwork.id == selectedRadioButoon){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0L,0f,this)
        }
        if(rbGPS.id == selectedRadioButoon){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0L,0f,this)
        }


    }
    private fun unregisterListener(){
        sensorManager.unregisterListener(this,sensorBeschleunigung)
        sensorManager.unregisterListener(this,sensorGyroskop)
        sensorManager.unregisterListener(this,sensorMagnometer)
        sensorManager.unregisterListener(this,sensorLicht)
        sensorManager.unregisterListener(this,sensorDruck)
        sensorManager.unregisterListener(this,sensorProxi)
        locationManager.removeUpdates(this)
    }

    override fun onSensorChanged(p0: SensorEvent?) {
       val jasonObjekt = JSONObject()
        if(p0!!.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION){
            getBeschleunigungsData(p0)
        }else if(p0.sensor.type == Sensor.TYPE_GYROSCOPE){
            getGyroskopData(p0)
        }else if(p0.sensor.type == Sensor.TYPE_MAGNETIC_FIELD){
            getMagnetoData(p0)
        }else if(p0.sensor.type == Sensor.TYPE_LIGHT){
            tvLicht.text = "${p0!!.values[0]}"
            jasonObjekt.put("SensorTyp","Licht")
            jasonObjekt.put("Lichtstarke",p0!!.values[0])
            jasonObjekt.put("TimeStamp",p0!!.timestamp)
            jsonArray.put(jasonObjekt)
        }else if(p0.sensor.type == Sensor.TYPE_PRESSURE){
            tvDruck.text = "${"%.2f".format(p0!!.values[0])}"
            jasonObjekt.put("SensorTyp","Bar")
            jasonObjekt.put("Druck",p0!!.values[0])
            jasonObjekt.put("TimeStamp",p0!!.timestamp)
            jsonArray.put(jasonObjekt)
        }else if(p0.sensor.type == Sensor.TYPE_PROXIMITY){
            tvProxi.text = "${p0!!.values[0]}"
            jasonObjekt.put("SensorTyp","Proximiter")
            jasonObjekt.put("Bildschirmabstand",p0!!.values[0])
            jasonObjekt.put("TimeStamp",p0!!.timestamp)
            jsonArray.put(jasonObjekt)
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

        val jasonObject = JSONObject()
        jasonObject.put("SensorTyp","Accelometer")
        jasonObject.put("X",beschleunigungsData!!.x)
        jasonObject.put("Y",beschleunigungsData!!.y)
        jasonObject.put("Z",beschleunigungsData!!.z)
        jasonObject.put("TimeStamp",beschleunigungsData!!.timestamp)
        jsonArray.put(jasonObject)


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
        val jasonObject = JSONObject()
        jasonObject.put("SensorTyp", "Gyroskop")
        jasonObject.put("X",gyroskopData!!.x)
        jasonObject.put("Y",gyroskopData!!.y)
        jasonObject.put("Z",gyroskopData!!.z)
        jasonObject.put("TimeStamp",gyroskopData!!.timestamp)
        jsonArray.put(jasonObject)
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
        val jasonObject = JSONObject()
        jasonObject.put("SensorTyp", "Magnetometer")
        jasonObject.put("X",magnometerData!!.x)
        jasonObject.put("Y",magnometerData!!.y)
        jasonObject.put("Z",magnometerData!!.z)
        jasonObject.put("TimeStamp",magnometerData!!.timestamp)
        jsonArray.put(jasonObject)
    }

    override fun onLocationChanged(p0: Location) {
        tvLati.text = "${"%.5f".format(p0.latitude)}"
        tvAlti.text = "${"%.5f".format(p0.altitude)}"
        tvLongi.text = "${"%.5f".format(p0.longitude)}"
        tvAccu.text = "${"%.2f".format(p0.accuracy)}"

        val jasonObjekt = JSONObject()
        jasonObjekt.put("SensorTyp",p0.provider)
        jasonObjekt.put("Latitude",p0.latitude)
        jasonObjekt.put("Longitude",p0.longitude)
        jasonObjekt.put("Altitude",p0.altitude)
        jasonObjekt.put("Accuracy",p0.accuracy)
        jasonObjekt.put("TimeStamp",p0.time)

        jsonArray.put(jasonObjekt)
    }


}