package fsi.fsicli;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Parse responses from FSI API. Uses the XML output just because that's the
 * default. JSON is also supported by the API.
 */
public class FsiResponseParser {
	private final DocumentBuilderFactory doc_factory;

	public FsiResponseParser() {
		this.doc_factory = DocumentBuilderFactory.newInstance();
	}

	public SaltResponse parseSaltResponse(InputStream is) throws IOException {
		Document doc;
		try {
			doc = doc_factory.newDocumentBuilder().parse(is);
		} catch (SAXException | ParserConfigurationException e) {
			throw new IOException(e);
		}

		Element root = doc.getDocumentElement();

		boolean success = get_child_text(root, "state").equals("success");
		String salt = get_child_text(root, "salt");

		return new SaltResponse(success, salt);
	}

	public LoginResponse parseLoginResponse(InputStream is) throws IOException {
		Document doc;
		try {
			doc = doc_factory.newDocumentBuilder().parse(is);
		} catch (SAXException | ParserConfigurationException e) {
			throw new IOException(e);
		}

		Element root = doc.getDocumentElement();

		boolean success = get_child_text(root, "state").equals("success");
		String user = get_child_text(root, "username");
		String msg = get_child_text(root, "message", false, "");

		return new LoginResponse(success, user, msg);
	}

	private static String get_child_text(Element parent, String name, boolean required, String default_text) {
		Element child = get_child(parent, name, required);

		if (!required && child == null) {
			return default_text;
		}

		return child.getTextContent();
	}

	private static String get_child_text(Element parent, String name) {
		return get_child_text(parent, name, true, null);
	}

	private static Element get_child(Element parent, String name, boolean required) {
		Optional<Element> result = stream_elements(parent.getElementsByTagName(name))
				.filter(n -> n.getNodeName().equals(name)).findFirst();

		if (result.isPresent()) {
			return result.get();
		}

		if (required) {
			throw new IllegalStateException("Required child " + name + " of " + parent.getNodeName() + " not found");
		}

		return null;
	}

	private static Stream<Element> stream_elements(NodeList nl) {
		Builder<Element> builder = Stream.builder();

		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);

			if (n instanceof Element) {
				builder.accept((Element) n);
			}
		}

		return builder.build();
	}

	private ImageInfo parse_info(Element image) {
		String path = get_child(image, "path", true).getAttribute("value");

		int width = Integer.parseInt(get_child(image, "width", true).getAttribute("value"));
		int height = Integer.parseInt(get_child(image, "height", true).getAttribute("value"));

		return new ImageInfo(path, width, height);
	}

	public List<ImageInfo> parseListResponse(InputStream is) throws IOException {
		List<ImageInfo> result = new ArrayList<>();

		Document doc;
		try {
			doc = doc_factory.newDocumentBuilder().parse(is);
		} catch (SAXException | ParserConfigurationException e) {
			throw new IOException(e);
		}

		Element images = get_child(doc.getDocumentElement(), "images", true);
				
		stream_elements(images.getChildNodes()).forEach(el -> result.add(parse_info(el)));

		return result;
	}
}
