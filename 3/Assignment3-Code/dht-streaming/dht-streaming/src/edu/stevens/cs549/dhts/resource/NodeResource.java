package edu.stevens.cs549.dhts.resource;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.SseFeature;

import edu.stevens.cs549.dhts.activity.DHTBase.Invalid;

@Path("/dht")
public class NodeResource {

	/*
	 * Web service API.
	 * 
	 * TODO: Fill in the missing operations.
	 */

	Logger log = Logger.getLogger(NodeResource.class.getCanonicalName());

	@Context
	UriInfo uriInfo;

	@Context
	HttpHeaders headers;

	@GET
	@Path("info")
	@Produces("application/xml")
	public Response getNodeInfoXML() {
		return new NodeService(headers, uriInfo).getNodeInfo();
	}

	@GET
	@Path("info")
	@Produces("application/json")
	public Response getNodeInfoJSON() {
		return new NodeService(headers, uriInfo).getNodeInfo();
	}

	@GET
	@Path("pred")
	@Produces("application/xml")
	public Response getPred() {
		return new NodeService(headers, uriInfo).getPred();
	}

	@GET
	@Path("succ")
	@Produces("application/xml")
	public Response getSucc() {
		return new NodeService(headers, uriInfo).getSucc();
	}
	
	@GET
	@Path("get")
	@Produces("application/xml")
	public Response getBinding(@QueryParam("key") String key) throws Invalid {
		return new NodeService(headers, uriInfo).getBinding(key);
	}
	
	@PUT
	@Path("add")
	@Consumes("application/xml")
	@Produces("application/xml")
	public Response addBinding(@QueryParam("key") String key, @QueryParam("val") String val) throws Invalid {
		return new NodeService(headers, uriInfo).addBinding(key, val);
	}
	
	@DELETE
	@Path("delete")
	@Produces("application/xml")
	public Response deleteBinding(@QueryParam("key") String key, @QueryParam("val") String val) throws Invalid {
		return new NodeService(headers, uriInfo).deleteBinding(key, val);
	}
	
	@GET
	@Path("finger")
	@Produces("application/xml")
	public Response getclosestPrecedingFinger(@QueryParam("id") String index) {
		int id = Integer.parseInt(index);
		return new NodeService(headers, uriInfo).closestPrecedingFinger(id);
	}
	
	@GET
	@Path("find")
	@Produces("application/xml")
	public Response findSuccessor(@QueryParam("id") String index) {
		int id = Integer.parseInt(index);
		return new NodeService(headers, uriInfo).findSuccessor(id);
	}

	@PUT
	@Path("notify")
	@Consumes("application/xml")
	@Produces("application/xml")
	/*
	 * Actually returns a TableRep (annotated with @XmlRootElement)
	 */
	public Response putNotify(TableRep predDb) {
		/*
		 * See the comment for WebClient::notify (the client side of this logic).
		 */
		return new NodeService(headers, uriInfo).notify(predDb);
		// NodeInfo p = predDb.getInfo();
	}
	
	@GET
	@Path("listen")
	@Produces(SseFeature.SERVER_SENT_EVENTS)
	public Response listenOn(@QueryParam("id") int id, @QueryParam("key") String key) {
		return new NodeService(headers, uriInfo).listen(id, key);
	}
	
	@DELETE
	@Path("listen")
	public Response listenOff(@QueryParam("id") int id, @QueryParam("key") String key) {
		return new NodeService(headers, uriInfo).stopListening(id, key);
	}

}
