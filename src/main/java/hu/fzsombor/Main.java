package hu.fzsombor;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

public class Main {

    public static void main(String[] args) {
        final int carNum = Integer.parseInt(args[1]);
        final String kafkaBrokers = args[2];

        // creating spark session
        SparkSession ss = SparkSession.builder().appName("ETLConsumer").getOrCreate();

        // setting schema for the json decode
        StructType schema = new StructType();
        schema.add("Id", DataTypes.FloatType);
        schema.add("coolantTemp", DataTypes.FloatType);
        schema.add("intakeAirTemp", DataTypes.FloatType);
        schema.add("intakeAirFlowSpeed", DataTypes.FloatType);
        schema.add("batteryPercentage", DataTypes.FloatType);
        schema.add("batteryVoltage", DataTypes.FloatType);
        schema.add("speed", DataTypes.FloatType);
        schema.add("engineVibrationAmplitude", DataTypes.FloatType);
        schema.add("throttlePos", DataTypes.FloatType);
        schema.add("tirePressure11", DataTypes.FloatType);
        schema.add("tirePressure12", DataTypes.FloatType);
        schema.add("tirePressure21", DataTypes.FloatType);
        schema.add("tirePressure22", DataTypes.FloatType);
        schema.add("accelerometer11Value", DataTypes.FloatType);
        schema.add("accelerometer12Value", DataTypes.FloatType);
        schema.add("accelerometer21Value", DataTypes.FloatType);
        schema.add("accelerometer22Value", DataTypes.FloatType);
        schema.add("controlUnitFirmware", DataTypes.IntegerType);
        schema.add("failureOccurred", DataTypes.BooleanType);

        // getting the kafka structured stream
        Dataset<Row> row = ss.readStream().format("kafka").option("kafka.bootstrap.servers", kafkaBrokers).option("subscribe", "carstream").load();

        // access the partition by checking the key
        if (row.selectExpr("CAST(key AS INT)").equals(carNum)){

           // parsing the json based on the schema provided before
           Dataset<Row> output = row.select(functions.from_json(row.col("value"), DataType.fromJson(schema.json())).as("data")).select("data.*");

           // writing the data as a Hive table in HMS
           output.write().option("path", "/user/hive/warehouse/carstream").saveAsTable("carstream");
       }
    }
}


