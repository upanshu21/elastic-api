package infectedservice

import akka.actor.ActorSystem
import infectedservice.ElasticSearchService._
import org.elasticsearch.action.delete.{DeleteRequest, DeleteResponse}
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.{SearchRequest, SearchResponse}
import org.elasticsearch.client.{RequestOptions, RestHighLevelClient}
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import com.knoldus.daml.routes.DamlData.InfectedPerson.InfectedPersonData
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.knoldus.daml.routes.DamlData.jsonProtocol.JsonProtocol
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import scala.concurrent.{ExecutionContextExecutor, Future}

object ElasticSearchService {

  val Index = "covid-data"
  val Type = "patient"
}

class ElasticSearchService(client: RestHighLevelClient)(implicit val system: ActorSystem, implicit val executionContext: ExecutionContextExecutor) extends JsonProtocol with SprayJsonSupport{

  def insert(infected: InfectedPersonData): Future[HttpResponse] = Future{
    val indexRequest = new IndexRequest(Index, Type, infected.id.toString)
    val jsonString = infected.toJson.toString
    indexRequest.source(jsonString, XContentType.JSON)
    client.index(indexRequest,RequestOptions.DEFAULT)
    HttpResponse(StatusCodes.OK)
  }

  def delete(id: String): DeleteResponse = {
    val deleteRequest = new DeleteRequest(Index, Type, id)
    client.delete(deleteRequest,RequestOptions.DEFAULT)
  }

  def searchByField(fieldName: String, value: Any): SearchResponse = {
    val searchRequest = new SearchRequest(Index)
    val searchSourceBuilder = new SearchSourceBuilder
    searchSourceBuilder.query(QueryBuilders.matchPhraseQuery(fieldName, value))
    searchRequest.source(searchSourceBuilder)
    client.search(searchRequest,RequestOptions.DEFAULT)
  }

}
