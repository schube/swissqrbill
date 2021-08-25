package ch.eugster.swissqrcode.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map.Entry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.eugster.swissqrcode.SwissQRBillGenerator;
import net.codecrete.qrbill.generator.GraphicsFormat;
import net.codecrete.qrbill.generator.Language;
import net.codecrete.qrbill.generator.OutputSize;

public class QRCodeTest 
{
	private String output;

	private String invoice;

	@BeforeEach
	public void beforeEach() throws URISyntaxException
	{
		String out = (System.getProperty("user.home") + File.separator + "bill.pdf");
		output = new File(out).toURI().toASCIIString();
		URI uri = QRCodeTest.class.getResource("/invoice.pdf").toURI();
		invoice = uri.toASCIIString();
	}
	
	@Test
	public void testWithInvoiceAsNumberAndDebtorNumber() throws JsonMappingException, JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		ObjectNode path = node.putObject("path");
		path.put("output", output);
		path.put("invoice", invoice);
		ObjectNode form = node.putObject("form");
		form.put("output_size", OutputSize.QR_BILL_EXTRA_SPACE.name());
		form.put("graphics_format", GraphicsFormat.PDF.name());
		form.put("language", Language.DE.name());
		node.put("iban", "CH4431999123000889012");
		node.put("amount", 199.95);
		node.put("currency", "CHF");
		node.put("invoice", 10456);
		ObjectNode creditor = node.putObject("creditor");
		creditor.put("name", "Robert Schneider AG");
		creditor.put("address", "Rue du Lac 1268/2/22");
		creditor.put("city", "2501 Biel");
		creditor.put("country", "CH");
		node.put("message", "Abonnement für 2020");
		ObjectNode debtor = node.putObject("debtor");
		debtor.put("number", 9048);
		debtor.put("name", "Pia-Maria Rutschmann-Schnyder");
		debtor.put("address", "Grosse Marktgasse 28");
		debtor.put("city", "9400 Rorschach");
		debtor.put("country", "CH");
		Object result = new SwissQRBillGenerator().generate(node.toString());
		assertEquals("OK", result);	
	}
	
	@Test
	public void testWithReference() throws JsonMappingException, JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		ObjectNode path = node.putObject("path");
		path.put("output", output);
		path.put("invoice", invoice);
		ObjectNode form = node.putObject("form");
		form.put("output_size", OutputSize.QR_BILL_EXTRA_SPACE.name());
		form.put("graphics_format", GraphicsFormat.PDF.name());
		form.put("language", Language.DE.name());
		node.put("iban", "CH4431999123000889012");
		node.put("amount", 199.95);
		node.put("currency", "CHF");
		ObjectNode creditor = node.putObject("creditor");
		creditor.put("name", "Robert Schneider AG");
		creditor.put("address", "Rue du Lac 1268/2/22");
		creditor.put("city", "2501 Biel");
		creditor.put("country", "CH");
		node.put("message", "Abonnement für 2020");
		ObjectNode debtor = node.putObject("debtor");
		debtor.put("name", "Pia-Maria Rutschmann-Schnyder");
		debtor.put("address", "Grosse Marktgasse 28");
		debtor.put("city", "9400 Rorschach");
		debtor.put("country", "CH");
		node.put("reference", "123451234567");
		Object result = new SwissQRBillGenerator().generate(node.toString());
		assertEquals("OK", result);	
	}
	
	@Test
	public void testWithoutExistingInvoiceToAppendTo() throws JsonMappingException, JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		ObjectNode path = node.putObject("path");
		path.put("output", output);
		ObjectNode form = node.putObject("form");
		form.put("output_size", OutputSize.QR_BILL_EXTRA_SPACE.name());
		form.put("graphics_format", GraphicsFormat.PDF.name());
		form.put("language", Language.DE.name());
		node.put("iban", "CH4431999123000889012");
		node.put("amount", 199.95);
		node.put("currency", "CHF");
		ObjectNode creditor = node.putObject("creditor");
		creditor.put("name", "Robert Schneider AG");
		creditor.put("address", "Rue du Lac 1268/2/22");
		creditor.put("city", "2501 Biel");
		creditor.put("country", "CH");
		node.put("message", "Abonnement für 2020");
		ObjectNode debtor = node.putObject("debtor");
		debtor.put("name", "Pia-Maria Rutschmann-Schnyder");
		debtor.put("address", "Grosse Marktgasse 28");
		debtor.put("city", "9400 Rorschach");
		debtor.put("country", "CH");
		Object result = new SwissQRBillGenerator().generate(node.toString());
		assertEquals("OK", result);	
	}
	
	@Test
	public void testMissingCreditor() throws JsonMappingException, JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		ObjectNode path = node.putObject("path");
		path.put("output", output);
		path.put("invoice", invoice);
		ObjectNode form = node.putObject("form");
		form.put("output_size", OutputSize.QR_BILL_EXTRA_SPACE.name());
		form.put("graphics_format", GraphicsFormat.PDF.name());
		form.put("language", Language.DE.name());
		node.put("invoice", "12345");
		node.put("iban", "CH4431999123000889012");
		node.put("amount", 199.95);
		node.put("currency", "CHF");
		node.put("reference", "3139471430009017");
		node.put("message", "Abonnement für 2020");
		ObjectNode debtor = node.putObject("debtor");
		debtor.put("number", "1234567");
		debtor.put("name", "Pia-Maria Rutschmann-Schnyder");
		debtor.put("address", "Grosse Marktgasse 28");
		debtor.put("city", "9400 Rorschach");
		debtor.put("country", "CH");
		Object result = new SwissQRBillGenerator().generate(node.toString());
		JsonNode resultNode = mapper.readTree(result.toString());
		assertEquals(ArrayNode.class, resultNode.getClass());
		Iterator<Entry<String, JsonNode>> entries = resultNode.fields();
		while (entries.hasNext())
		{
			Entry<String, JsonNode> next = entries.next();
			if (next.getKey().equals("reference"))
			{
				assertEquals("mandatory_for_qr_iban", next.getValue());
			}
			else
			{
				assertEquals("field_is_mandatory", next.getValue());
			}
		}
	}
	
	@Test
	public void testMissingDebtor() throws JsonMappingException, JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		ObjectNode path = node.putObject("path");
		path.put("output", output);
		path.put("invoice", invoice);
		ObjectNode form = node.putObject("form");
		form.put("output_size", OutputSize.QR_BILL_EXTRA_SPACE.name());
		form.put("graphics_format", GraphicsFormat.PDF.name());
		form.put("language", Language.DE.name());
		ObjectNode creditor = node.putObject("creditor");
		creditor.put("name", "Robert Schneider AG");
		creditor.put("address", "Rue du Lac 1268/2/22");
		creditor.put("city", "2501 Biel");
		creditor.put("country", "CH");
		node.put("invoice", "12345");
		node.put("iban", "CH4431999123000889012");
		node.put("amount", 199.95);
		node.put("currency", "CHF");
		node.put("reference", "3139471430009017");
		node.put("message", "Abonnement für 2020");
		Object result = new SwissQRBillGenerator().generate(node.toString());
		assertEquals("OK", result.toString());
	}
	
	@Test
	public void testWithoutIban() throws JsonMappingException, JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		ObjectNode path = node.putObject("path");
		path.put("output", output);
		path.put("invoice", invoice);
		ObjectNode form = node.putObject("form");
		form.put("output_size", OutputSize.QR_BILL_EXTRA_SPACE.name());
		form.put("graphics_format", GraphicsFormat.PDF.name());
		form.put("language", Language.DE.name());
		node.put("amount", 199.95);
		node.put("currency", "CHF");
		ObjectNode creditor = node.putObject("creditor");
		creditor.put("name", "Robert Schneider AG");
		creditor.put("address", "Rue du Lac 1268/2/22");
		creditor.put("city", "2501 Biel");
		creditor.put("country", "CH");
		node.put("message", "Abonnement für 2020");
		ObjectNode debtor = node.putObject("debtor");
		debtor.put("name", "Pia-Maria Rutschmann-Schnyder");
		debtor.put("address", "Grosse Marktgasse 28");
		debtor.put("city", "9400 Rorschach");
		debtor.put("country", "CH");
		Object result = new SwissQRBillGenerator().generate(node.toString());
		JsonNode resultNode = mapper.readTree(result.toString());
		assertEquals(ArrayNode.class, resultNode.getClass());
		assertEquals(1, resultNode.size());
		Iterator<Entry<String, JsonNode>> entries = resultNode.fields();
		while (entries.hasNext())
		{
			Entry<String, JsonNode> next = entries.next();
			assertEquals("account", next.getKey());
			assertEquals("field_is_mandatory", next.getValue());
		}
	}

	@Test
	public void testDocumentToAppendToDoesNotExist() throws JsonMappingException, JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		ObjectNode path = node.putObject("path");
		path.put("output", output);
		path.put("invoice", "C:/Users/christian/x.pdf");
		ObjectNode form = node.putObject("form");
		form.put("output_size", OutputSize.QR_BILL_EXTRA_SPACE.name());
		form.put("graphics_format", GraphicsFormat.PDF.name());
		form.put("language", Language.DE.name());
		node.put("iban", "CH4431999123000889012");
		node.put("amount", 199.95);
		node.put("currency", "CHF");
		node.put("invoice", 10456);
		ObjectNode creditor = node.putObject("creditor");
		creditor.put("name", "Robert Schneider AG");
		creditor.put("address", "Rue du Lac 1268/2/22");
		creditor.put("city", "2501 Biel");
		creditor.put("country", "CH");
		node.put("message", "Abonnement für 2020");
		ObjectNode debtor = node.putObject("debtor");
		debtor.put("number", 9048);
		debtor.put("name", "Pia-Maria Rutschmann-Schnyder");
		debtor.put("address", "Grosse Marktgasse 28");
		debtor.put("city", "9400 Rorschach");
		debtor.put("country", "CH");
		Object result = new SwissQRBillGenerator().generate(node.toString());
		JsonNode resultNode = mapper.readTree(result.toString());
		assertEquals(ArrayNode.class, resultNode.getClass());
		assertEquals(1, resultNode.size());
		JsonNode entry = resultNode.get(0).get("io_exception");
		assertEquals("Source path 'C:\\Users\\christian\\x.pdf' does not exist", entry.asText());
	}
	
	@Test
	public void testWithBillAsPng() throws JsonMappingException, JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		ObjectNode path = node.putObject("path");
		path.put("output", output);
		path.put("invoice", invoice);
		ObjectNode form = node.putObject("form");
		form.put("output_size", OutputSize.QR_BILL_EXTRA_SPACE.name());
		form.put("graphics_format", GraphicsFormat.PNG.name());
		form.put("language", Language.DE.name());
		node.put("iban", "CH4431999123000889012");
		node.put("amount", 199.95);
		node.put("currency", "CHF");
		node.put("invoice", 10456);
		ObjectNode creditor = node.putObject("creditor");
		creditor.put("name", "Robert Schneider AG");
		creditor.put("address", "Rue du Lac 1268/2/22");
		creditor.put("city", "2501 Biel");
		creditor.put("country", "CH");
		node.put("message", "Abonnement für 2020");
		ObjectNode debtor = node.putObject("debtor");
		debtor.put("number", 9048);
		debtor.put("name", "Pia-Maria Rutschmann-Schnyder");
		debtor.put("address", "Grosse Marktgasse 28");
		debtor.put("city", "9400 Rorschach");
		debtor.put("country", "CH");
		Object result = new SwissQRBillGenerator().generate(node.toString());
		assertEquals("OK", result);	
	}
	
}
