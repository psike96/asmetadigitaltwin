using System;
using System.Text;
using Microsoft.Azure.ServiceBus;
using Microsoft.Azure.WebJobs;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json.Linq;
using Newtonsoft.Json;
using MXCHIPSensorUpdater.DigitalTwins;

namespace MXCHIPSensorUpdater
{
    public class IoTCentralMsgTrigger
    {
        const string queueName = "iotcentralmessages";

        [FunctionName("IoTCentralMsgTrigger")]
        public void Run([ServiceBusTrigger(queueName, Connection = "ServiceBusConnection")]
            Message message,
            ILogger log)
        {
            string sensorId = message.UserProperties["iotcentral-device-id"].ToString();
            string value = Encoding.ASCII.GetString(message.Body, 0, message.Body.Length);
            var bodyProperty = (JObject)JsonConvert.DeserializeObject(value);
            JToken temperatureToken = bodyProperty["telemetry"]["temperature"];
            float temperature = temperatureToken.Value<float>();
            log.LogInformation(string.Format("Sensor Id:{0}", sensorId));
            log.LogInformation(string.Format("Sensor Temperature:{0}", temperature));
            DigitalTwinsManager manager = new();
            manager.UpdateDigitalTwinProperty(sensorId, "temperature", temperature);
        }
    }
}