/* 
 * $Id: JGraphpadSVGServer.java,v 1.2 2006/01/31 12:10:34 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.svgplugin;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.jgraph.JGraph;
import org.jgraph.graph.GraphLayoutCache;

import com.jgraph.JGraphEditor;
import com.jgraph.JGraphpad;
import com.jgraph.editor.JGraphEditorDiagram;
import com.jgraph.editor.JGraphEditorModel;
import com.jgraph.editor.JGraphEditorResources;
import com.jgraph.pad.util.JGraphpadImageEncoder;

/**
 * Simple webserver to stream SVG, PNG and JPG content to clients. This
 * implementation is based on nanoHttpd (http://nanohttpd.sourceforge.net/).
 */
public class JGraphpadSVGServer {

	/**
	 * Some HTTP response status codes
	 */
	public static final String HTTP_OK = "200 OK",
			HTTP_REDIRECT = "301 Moved Permanently",
			HTTP_FORBIDDEN = "403 Forbidden", HTTP_NOTFOUND = "404 Not Found",
			HTTP_BADREQUEST = "400 Bad Request",
			HTTP_INTERNALERROR = "500 Internal Server Error",
			HTTP_NOTIMPLEMENTED = "501 Not Implemented";

	/**
	 * Common mime types for dynamic content
	 */
	public static final String MIME_PLAINTEXT = "text/plain",
			MIME_HTML = "text/html",
			MIME_DEFAULT_BINARY = "application/octet-stream",
			MIME_PNG = "image/png", MIME_JPG = "image/jpeg";

	/**
	 * Holds the socket the server is listening on.
	 */
	protected ServerSocket serverSocket;

	/**
	 * References the enclosing editor.
	 */
	protected JGraphEditor editor;

	/**
	 * Starts a HTTP server for the enclosing editor on the specified port.
	 * <p>
	 * Throws an IOException if the socket is already in use
	 */
	public JGraphpadSVGServer(JGraphEditor editor, int port) throws IOException {
		this.editor = editor;
		serverSocket = new ServerSocket(port);
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					while (true)
						new HTTPSession(serverSocket.accept());
				} catch (IOException ioe) {
					// ignore
				}
			}
		});
		t.setDaemon(true);
		t.start();
	}

	/**
	 * Serves the response for the specified request. This returns a HTML index
	 * containing the links to the diagrams in the editor's document model, or
	 * one of the diagrams as an SVG, JPG or PNG image.
	 * 
	 * @param uri
	 *            Percent-decoded URI without parameters, for example
	 *            "/index.cgi"
	 * @param method
	 *            "GET", "POST" etc.
	 * @param parms
	 *            Parsed, percent decoded parameters from URI and, in case of
	 *            POST, data.
	 * @param header
	 *            Header entries, percent decoded
	 * @return HTTP response, see class Response for details
	 * @throws IOException
	 */
	public Response serve(String uri, String method, Properties header,
			Properties parms) throws IOException {
		Response response = null;
		String format = parms.getProperty("format");
		JGraph graph = getGraph(uri);
		if (uri.equals("/"))
			response = serveIndex();
		else if (graph != null) {

			// Responds with a simple HTML info if the graph is empty
			if (graph.getModel().getRootCount() == 0)
				response = new Response(HTTP_OK, MIME_HTML,
						JGraphEditorResources.getString("GraphContainsNoData"));

			// Otherwise responds with the image in the requested format
			else if (format == null)
				response = serveSVG(graph);
			else if (JGraphpad.isImage(format))
				response = serveImage(graph, format);
		}
		return response;
	}

	/**
	 * Produces an SVG image of the specified graph.
	 */
	protected Response serveSVG(JGraph graph) throws IOException {
		OutputStream out = new ByteArrayOutputStream();
		JGraphpadSVGAction.writeSVG(graph, out, 10);
		out.close();
		return new Response(HTTP_OK, JGraphpadSVGAction.MIME_SVG, out
				.toString());
	}

	/**
	 * Produces a JPG or PNG image of the specified graph.
	 */
	protected Response serveImage(JGraph graph, String format)
			throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BufferedImage img = graph.getImage(Color.white, 5);
		if (format.equalsIgnoreCase("gif"))
			JGraphpadImageEncoder.writeGIF(img, out);
		else
			ImageIO.write(img, format, out);
		out.close();
		return new Response(HTTP_OK,
				(format.equalsIgnoreCase("png")) ? MIME_PNG : MIME_JPG, out
						.toByteArray());
	}

	/**
	 * Produces a HTML index page of all diagrams in the document model.
	 */
	protected Response serveIndex() {
		String content = "";
		JGraphEditorModel model = (JGraphEditorModel) editor.getModel();
		Object root = model.getRoot();
		int childCount = model.getChildCount(root);
		if (childCount == 0)
			content += JGraphEditorResources.getString("NoDocument");
		for (int i = 0; i < model.getChildCount(root); i++) {
			Object child = model.getChild(root, i);
			for (int j = 0; j < model.getChildCount(child); j++) {
				Object diagram = model.getChild(child, j);
				String label = String.valueOf(child) + "."
						+ String.valueOf(diagram);
				content += label + ":&nbsp;";
				content += "<a href=\"" + String.valueOf(i) + "/"
						+ String.valueOf(j) + "/\">SVG</a>&nbsp;";
				content += "<a href=\"" + String.valueOf(i) + "/"
						+ String.valueOf(j) + "?format=png\">PNG</a>&nbsp;";
				content += "<a href=\"" + String.valueOf(i) + "/"
						+ String.valueOf(j) + "?format=jpg\">JPG</a>&nbsp;";
				content += "<a href=\"" + String.valueOf(i) + "/"
						+ String.valueOf(j) + "?format=gif\">GIF</a></br>";
			}
		}
		return new Response(HTTP_OK, MIME_HTML, content);
	}

	/**
	 * Returns a JGraph for the specified reference. The refence is of the form
	 * i/j/k/... where i is the index of the first parent in the model, j is the
	 * index of the child etc. If the model element is a
	 * {@link JGraphEditorDiagram} then a graph is created using
	 * {@link #createGraph(GraphLayoutCache)} and returned.
	 */
	protected JGraph getGraph(String reference) {
		try {
			Object parent = editor.getModel().getRoot();
			String[] path = reference.substring(1).split("/");
			for (int i = 0; i < path.length; i++)
				parent = editor.getModel().getChild(parent,
						Integer.parseInt(path[i]));
			if (parent instanceof JGraphEditorDiagram) {
				GraphLayoutCache cache = ((JGraphEditorDiagram) parent)
						.getGraphLayoutCache();
				return createGraph(cache);
			}
		} catch (Exception e) {
			// ignore
		}
		return null;
	}

	/**
	 * Creates a new graph for the specified cache and puts it into the
	 * backingFrame component hierachy.
	 */
	protected JGraph createGraph(GraphLayoutCache cache) {
		JGraph graph = editor.getFactory().createGraph(cache);
		// "Headless Swing Hack"
		JPanel panel = new JPanel();
		panel.setDoubleBuffered(false);
		panel.add(graph);
		panel.setVisible(true); // required
		panel.setEnabled(true); // also required
		panel.addNotify(); // simlutes pack on jframe
		panel.validate();
		return graph;
	}

	/**
	 * HTTP response. Return one of these from serve().
	 */
	public class Response {
		/**
		 * Default constructor: response = HTTP_OK, data = mime = 'null'
		 */
		public Response() {
			this.status = HTTP_OK;
		}

		/**
		 * Basic constructor.
		 */
		public Response(String status, String mimeType, InputStream data) {
			this.status = status;
			this.mimeType = mimeType;
			this.data = data;
		}

		/**
		 * Convenience method that makes an InputStream out of given text.
		 */
		public Response(String status, String mimeType, String txt) {
			this(status, mimeType, txt.getBytes());
		}

		/**
		 * Convenience method that makes an InputStream out of given text.
		 */
		public Response(String status, String mimeType, byte[] bytes) {
			this.status = status;
			this.mimeType = mimeType;
			this.data = new ByteArrayInputStream(bytes);
		}

		/**
		 * Convenience method that makes an InputStream out of given text.
		 */
		public Response(String status, String mimeType, OutputStream out) {
			this.status = status;
			this.mimeType = mimeType;
		}

		/**
		 * Adds given line to the header.
		 */
		public void addHeader(String name, String value) {
			header.put(name, value);
		}

		/**
		 * HTTP status code after processing, eg "200 OK", HTTP_OK
		 */
		public String status;

		/**
		 * MIME type of content, e.g. "text/html"
		 */
		public String mimeType;

		/**
		 * Data of the response, may be null.
		 */
		public InputStream data;

		/**
		 * Headers for the HTTP response. Use addHeader() to add lines.
		 */
		public Properties header = new Properties();
	}

	/**
	 * Handles one session, i.e. parses the HTTP request and returns the
	 * response.
	 */
	private class HTTPSession implements Runnable {
		public HTTPSession(Socket s) {
			mySocket = s;
			Thread t = new Thread(this);
			t.setDaemon(true);
			t.start();
		}

		public void run() {
			try {
				InputStream is = mySocket.getInputStream();
				if (is == null)
					return;
				BufferedReader in = new BufferedReader(
						new InputStreamReader(is));

				// Read the request line
				StringTokenizer st = new StringTokenizer(in.readLine());
				if (!st.hasMoreTokens())
					sendError(HTTP_BADREQUEST,
							"BAD REQUEST: Syntax error. Usage: GET /example/file.html");

				String method = st.nextToken();

				if (!st.hasMoreTokens())
					sendError(HTTP_BADREQUEST,
							"BAD REQUEST: Missing URI. Usage: GET /example/file.html");

				String uri = decodePercent(st.nextToken());

				// Decode parameters from the URI
				Properties parms = new Properties();
				int qmi = uri.indexOf('?');
				if (qmi >= 0) {
					decodeParms(uri.substring(qmi + 1), parms);
					uri = decodePercent(uri.substring(0, qmi));
				}

				// If there's another token, it's protocol version,
				// followed by HTTP headers. Ignore version but parse headers.
				Properties header = new Properties();
				if (st.hasMoreTokens()) {
					String line = in.readLine();
					while (line.trim().length() > 0) {
						int p = line.indexOf(':');
						header.put(line.substring(0, p).trim(), line.substring(
								p + 1).trim());
						line = in.readLine();
					}
				}

				// If the method is POST, there may be parameters
				// in data section, too, read another line:
				if (method.equalsIgnoreCase("POST"))
					decodeParms(in.readLine(), parms);

				// Ok, now do the serve()
				Response r = serve(uri, method, header, parms);
				if (r == null)
					sendError(HTTP_INTERNALERROR,
							"SERVER INTERNAL ERROR: Serve() returned a null response.");
				else
					sendResponse(r.status, r.mimeType, r.header, r.data);

				in.close();
			} catch (Exception ioe) {
				try {
					sendError(HTTP_INTERNALERROR,
							"SERVER INTERNAL ERROR: IOException: "
									+ ioe.getMessage());
				} catch (Throwable t) {
				}
			}
		}

		/**
		 * Decodes the percent encoding scheme. <br/>For example:
		 * "an+example%20string" -> "an example string"
		 */
		private String decodePercent(String str) throws InterruptedException {
			try {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < str.length(); i++) {
					char c = str.charAt(i);
					switch (c) {
					case '+':
						sb.append(' ');
						break;
					case '%':
						sb.append((char) Integer.parseInt(str.substring(i + 1,
								i + 3), 16));
						i += 2;
						break;
					default:
						sb.append(c);
						break;
					}
				}
				return new String(sb.toString().getBytes());
			} catch (Exception e) {
				sendError(HTTP_BADREQUEST, "BAD REQUEST: Bad percent-encoding.");
				return null;
			}
		}

		/**
		 * Decodes parameters in percent-encoded URI-format ( e.g.
		 * "name=Jack%20Daniels&pass=Single%20Malt" ) and adds them to given
		 * Properties.
		 */
		private void decodeParms(String parms, Properties p)
				throws InterruptedException {
			if (parms == null)
				return;

			StringTokenizer st = new StringTokenizer(parms, "&");
			while (st.hasMoreTokens()) {
				String e = st.nextToken();
				int sep = e.indexOf('=');
				if (sep >= 0)
					p.put(decodePercent(e.substring(0, sep)).trim(),
							decodePercent(e.substring(sep + 1)));
			}
		}

		/**
		 * Returns an error message as a HTTP response and throws
		 * InterruptedException to stop furhter request processing.
		 */
		private void sendError(String status, String msg)
				throws InterruptedException {
			sendResponse(status, MIME_PLAINTEXT, null,
					new ByteArrayInputStream(msg.getBytes()));
			throw new InterruptedException();
		}

		/**
		 * Sends given response to the socket.
		 */
		private void sendResponse(String status, String mime,
				Properties header, InputStream data) {
			try {
				if (status == null)
					throw new Error("sendResponse(): Status can't be null.");

				OutputStream out = mySocket.getOutputStream();
				PrintWriter pw = new PrintWriter(out);
				pw.print("HTTP/1.0 " + status + " \r\n");

				if (mime != null)
					pw.print("Content-Type: " + mime + "\r\n");

				if (header == null || header.getProperty("Date") == null)
					pw.print("Date: " + gmtFrmt.format(new Date()) + "\r\n");

				if (header != null) {
					Enumeration e = header.keys();
					while (e.hasMoreElements()) {
						String key = (String) e.nextElement();
						String value = header.getProperty(key);
						pw.print(key + ": " + value + "\r\n");
					}
				}

				pw.print("\r\n");
				pw.flush();

				if (data != null) {
					byte[] buff = new byte[2048];
					int read = 2048;
					while (read == 2048) {
						read = data.read(buff, 0, 2048);
						out.write(buff, 0, read);
					}
				}
				out.flush();
				out.close();
				if (data != null)
					data.close();
			} catch (IOException ioe) {
				// Couldn't write? No can do.
				try {
					mySocket.close();
				} catch (Throwable t) {
				}
			}
		}

		private Socket mySocket;

		private BufferedReader myIn;
	};

	/**
	 * URL-encodes everything between "/"-characters. Encodes spaces as '%20'
	 * instead of '+'.
	 */
	private String encodeUri(String uri) {
		String newUri = "";
		StringTokenizer st = new StringTokenizer(uri, "/ ", true);
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			if (tok.equals("/"))
				newUri += "/";
			else if (tok.equals(" "))
				newUri += "%20";
			else
				newUri += URLEncoder.encode(tok);
		}
		return newUri;
	}

	/**
	 * GMT date formatter
	 */
	private static java.text.SimpleDateFormat gmtFrmt;
	static {
		gmtFrmt = new java.text.SimpleDateFormat(
				"E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
		gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * @return Returns the serverSocket.
	 */
	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	/**
	 * @param serverSocket
	 *            The serverSocket to set.
	 */
	public void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

}