using Azure.Core.Pipeline;
using Azure.DigitalTwins.Core;
using Azure.Identity;
using Azure;
using System;
using System.Collections.Generic;
using System.IO;
using System.Net.Http;

namespace MXCHIPSensorUpdater.DigitalTwins
{
    public class DigitalTwinsManager
    {
        private static readonly string adtInstanceUrl = ""; // Azure Digital Twins endpoint
        private DigitalTwinsClient client;
        public delegate void QueryResult(BasicDigitalTwin dt);
        public DigitalTwinsManager()
        {
            Connect();
        }
        public DigitalTwinsManager(string appId)
        {
            ManagedConnect(appId);
        }
        public void Connect()
        {
            var cred = new DefaultAzureCredential();
            client = new DigitalTwinsClient(new Uri(adtInstanceUrl), cred);
        }
        public void ManagedConnect(string appId)
        {
            HttpClient httpClient = new();
            var cred = new ManagedIdentityCredential(appId);
            client = new DigitalTwinsClient(
                new Uri(adtInstanceUrl),
                cred,
                new DigitalTwinsClientOptions { Transport = new HttpClientTransport(httpClient) }
            );
        }
        public bool CreateModels(string[] path)
        {
            List<string> dtdls = new List<string>();
            foreach (string p in path)
            {
                using var modelStreamReader = new StreamReader(p);
                string dtdl = modelStreamReader.ReadToEnd();
                dtdls.Add(dtdl);
            }
            try
            {
                DigitalTwinsModelData[] models = client.CreateModels(dtdls.ToArray());
            }
            catch (RequestFailedException)
            {
                return false;
            }
            return true;
        }
        public bool CreateModel(string path)
        {
            return CreateModels(new string[] { path });
        }
        public void DeleteModel(string modelId)
        {
            client.DeleteModel(modelId);
        }
        public DigitalTwinsModelData GetModel(string modelId)
        {
            try
            {
                return client.GetModel(modelId);
            }
            catch (RequestFailedException)
            {
#pragma warning disable CS8603 // Possibile restituzione di riferimento Null.
                return null;
            }
        }
        public Pageable<DigitalTwinsModelData> GetModels()
        {
            GetModelsOptions options = new();
            return client.GetModels(options);
        }
        public bool CreateDigitalTwin(string twinId, string modelId)
        {
            BasicDigitalTwin digitalTwin = new();
            digitalTwin.Metadata = new DigitalTwinMetadata();
            digitalTwin.Metadata.ModelId = modelId;
            digitalTwin.Id = twinId;
            try
            {
                client.CreateOrReplaceDigitalTwin<BasicDigitalTwin>(twinId, digitalTwin);
            }
            catch (RequestFailedException)
            {
                return false;
            }
            return true;
        }
        public bool UpdateDigitalTwin(string twinId, string property, object value)
        {
            try
            {
                BasicDigitalTwin digitalTwin = client.GetDigitalTwin<BasicDigitalTwin>(twinId);
                digitalTwin.Contents[property] = value;
                client.CreateOrReplaceDigitalTwin<BasicDigitalTwin>(twinId, digitalTwin);
            }
            catch (RequestFailedException)
            {
                return false;
            }
            return true;
        }
        public bool DeleteDigitalTwin(string twinId)
        {
            try
            {
                client.DeleteDigitalTwin(twinId);
            }
            catch (RequestFailedException)
            {
                return false;
            }
            return true;
        }
        public BasicDigitalTwin GetDigitalTwin(string twinId)
        {
            try
            {
                return client.GetDigitalTwin<BasicDigitalTwin>(twinId);
            }
            catch (RequestFailedException)
            {
                return null;
            }
        }
        public void UpdateDigitalTwinProperty(string twinId, string property, object value)
        {
            JsonPatchDocument? patch;
            try
            {
                patch = new JsonPatchDocument();
                patch.AppendAdd("/" + property, value);
                client.UpdateDigitalTwin(twinId, patch);
            }
            catch (RequestFailedException)
            {
            }
            patch = new JsonPatchDocument();
            patch.AppendReplace("/" + property, value);
            client.UpdateDigitalTwin(twinId, patch);
        }
        public void CreateRelationship(string twinSourceId, string twinDestinationId, string description, Dictionary<string, object> properties = null)
        {
            string relationshipId = RelationshipId(twinSourceId, twinDestinationId);
            BasicRelationship relationship = new BasicRelationship
            {
                Id = "buildingFloorRelationshipId",
                SourceId = twinSourceId,
                TargetId = twinDestinationId,
                Name = description,
                Properties = properties
            };

            try
            {
                client.CreateOrReplaceRelationship(twinSourceId, relationshipId, relationship);
            }
            catch (RequestFailedException)
            {
            }
        }
        public string RelationshipId(string twinSourceId, string twinDestinationId)
        {
            return string.Format("{0}-{1}", twinSourceId, twinDestinationId);
        }
        public BasicRelationship GetRelationship(string twinSourceId, string twinDestinationId)
        {
            string relationshipId = RelationshipId(twinSourceId, twinDestinationId);
            try
            {
                Response<BasicRelationship> relationship = client.GetRelationship<BasicRelationship>(twinSourceId, relationshipId);
                return relationship;
            }
            catch (RequestFailedException)
            {
            }
            return null;
        }
        public Pageable<BasicRelationship> ListRelationships(string twinSourceId)
        {
            try
            {
                Pageable<BasicRelationship> relationships = client
                    .GetRelationships<BasicRelationship>(twinSourceId);
                return relationships;
            }
            catch (RequestFailedException)
            {
            }
            return null;
        }
        public void UpdateRelationship(string twinSourceId, string twinDestinationId, string property, object value)
        {
            string relationshipId = RelationshipId(twinSourceId, twinDestinationId);

            JsonPatchDocument patch = null;
            try
            {
                patch = new();
                patch.AppendReplace("/" + property, value);
                client.UpdateRelationship(twinSourceId, relationshipId, patch);
            }
            catch (RequestFailedException)
            {
            }
        }
        public Pageable<BasicDigitalTwin> QueryDigitalTwins(string query)
        {
            Pageable<BasicDigitalTwin> result = null;
            try
            {
                result = client.Query<BasicDigitalTwin>(query);
            }
            catch (RequestFailedException)
            {
            }
            return result;
        }
        public Pageable<Dictionary<string, BasicDigitalTwin>> Query(string query)
        {
            Pageable<Dictionary<string, BasicDigitalTwin>> result = null;
            try
            {
                result = client.Query<Dictionary<string, BasicDigitalTwin>>(query);
            }
            catch (RequestFailedException)
            {
            }
            return result;
        }
        public void QueryDigitalTwins(string query, QueryResult onQueryResult)
        {
            System.Threading.Tasks.Task task = System.Threading.Tasks.Task.Run(
                () => QueryDigitalTwinsAsync(query, onQueryResult)
            );
        }

        public async void QueryDigitalTwinsAsync(string query, QueryResult onQueryResult)
        {
            AsyncPageable<BasicDigitalTwin> result = client
                .QueryAsync<BasicDigitalTwin>(query);
            try
            {
                await foreach (BasicDigitalTwin dt in result)
                {
                    onQueryResult(dt);
                }
            }
            catch (RequestFailedException)
            {
            }
        }
    }
}
