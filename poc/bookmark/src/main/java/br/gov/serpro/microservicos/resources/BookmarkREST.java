package br.gov.serpro.microservicos.resources;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import br.gov.serpro.microservicos.logical.BookmarkBC;
import br.gov.serpro.microservicos.model.Bookmark;

@Path("bookmark")
public class BookmarkREST {

	@Inject
	private BookmarkBC bc;

	@Inject
	private Validator validator;

	@GET
	@Produces("application/json")
	public List<Bookmark> find(@QueryParam("q") String query) throws Exception {
		List<Bookmark> result;

		if (query == null || "".equals(query)) {
			result = bc.findAll();
		} else {
			result = bc.find(query);
		}

		return result;
	}

	@GET
	@Path("{id}")
	@Produces("application/json")
	public Bookmark load(@PathParam("id") Long id) throws Exception {
		Bookmark result = bc.load(id);

		if (result == null) {
			throw new NotFoundException();
		}

		return result;
	}

	@POST
	@Transactional
	@Produces("application/json")
	@Consumes("application/json")
	public Response insert(Bookmark body, @Context UriInfo uriInfo) throws Exception {

		ResponseBuilder builder = null;

		try {
			validateMember(body);
			String id = bc.insert(body).getId().toString();
			URI location = uriInfo.getRequestUriBuilder().path(id).build();
			builder = Response.created(location).entity(id);
		} catch (ConstraintViolationException ce) {
			builder = createViolationResponse(ce.getConstraintViolations());
		} catch (Exception e) {
			// Handle generic exceptions
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("error", e.getMessage());
			builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
		}

		return builder.build();
	}

	private void validateMember(Bookmark member) throws ConstraintViolationException {
		Set<ConstraintViolation<Bookmark>> violations = validator.validate(member);

		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
		}
	}

	private Response.ResponseBuilder createViolationResponse(Set<ConstraintViolation<?>> violations) {
		Map<String, String> responseObj = new HashMap<>();
		for (ConstraintViolation<?> violation : violations) {
			responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
		}
		return Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
	}
	
	@DELETE
	@Path("{id}")
	@Transactional
	public Response delete(@PathParam("id") Long id) throws Exception {
		ResponseBuilder builder = null;
		try{
			bc.delete(id);
			builder = Response.status(Response.Status.OK);
		} catch (NotFoundException e) {
			builder = Response.status(Response.Status.NOT_FOUND);
		}
		return builder.build();
	}

}
