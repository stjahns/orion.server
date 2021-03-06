package org.eclipse.orion.internal.server.servlets.project;

import java.io.IOException;
import java.net.URI;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.core.runtime.*;
import org.eclipse.orion.internal.server.servlets.ProtocolConstants;
import org.eclipse.orion.internal.server.servlets.ServletResourceHandler;
import org.eclipse.orion.server.core.ServerStatus;
import org.eclipse.orion.server.core.project.Project;
import org.eclipse.orion.server.servlets.OrionServlet;
import org.json.JSONException;
import org.json.JSONObject;

public class ProjectHandlerV1 extends ServletResourceHandler<Project> {

	final ServletResourceHandler<IStatus> statusHandler;

	/**
	 * @param statusHandler
	 */
	public ProjectHandlerV1(ServletResourceHandler<IStatus> statusHandler) {
		super();
		this.statusHandler = statusHandler;
	}

	@Override
	public boolean handleRequest(HttpServletRequest request, HttpServletResponse response, Project project) throws ServletException {

		switch (getMethod(request)) {
			case GET :
				handleGet(request, response, project);
				return true;
		}
		return false;
	}

	private void handleGet(HttpServletRequest request, HttpServletResponse response, Project project) throws ServletException {
		if (project == null || !project.exists()) {
			statusHandler.handleRequest(request, response, new ServerStatus(IStatus.ERROR, HttpServletResponse.SC_NOT_FOUND, "Could not find project", null));
			return;
		}
		try {
			JSONObject projectJson = project.toJson();
			URI uri = getURI(request);
			String pathInfo = request.getPathInfo();
			Path path = new Path(pathInfo);
			projectJson.put(ProtocolConstants.KEY_LOCATION, uri);
			projectJson.put(ProtocolConstants.KEY_CONTENT_LOCATION, URIUtil.append(URIUtil.append(uri.resolve("/file"), path.segment(0)), path.segment(1)).toString() + "/?depth=1");
			OrionServlet.writeJSONResponse(request, response, projectJson);
		} catch (JSONException e) {
			statusHandler.handleRequest(request, response, new ServerStatus(IStatus.ERROR, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error while creating response", e));
		} catch (IOException e) {
			statusHandler.handleRequest(request, response, new ServerStatus(IStatus.ERROR, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error while creating response", e));
		}
	}
}
