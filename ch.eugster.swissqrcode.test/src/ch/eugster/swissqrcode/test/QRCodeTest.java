package ch.eugster.swissqrcode.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.eugster.swissqrcode.SwissQRBillGenerator;
import net.codecrete.qrbill.generator.GraphicsFormat;
import net.codecrete.qrbill.generator.Language;
import net.codecrete.qrbill.generator.OutputSize;

public class QRCodeTest 
{
	private final String output = (System.getProperty("user.home") + File.separator + "bill.pdf").replace("\\", "/");

	@Test
	public void testMissingOutputSize() throws JsonMappingException, JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("output", output);
		node.put("graphics_format", GraphicsFormat.PDF.name());
		node.put("language", Language.DE.name());
		node.put("iban", "CH4431999123000889012");
		node.put("amount", 199.95);
		node.put("currency", "CHF");
		ObjectNode creditor = node.putObject("creditor");
		creditor.put("name", "Robert Schneider AG");
		creditor.put("address", "Rue du Lac 1268/2/22");
		creditor.put("city", "2501 Biel");
		creditor.put("country", "CH");
		node.put("reference", "210000000003139471430009017");
		node.put("message", "Abonnement für 2020");
		ObjectNode debtor = node.putObject("debtor");
		debtor.put("name", "Pia-Maria Rutschmann-Schnyder");
		debtor.put("address", "Grosse Marktgasse 28");
		debtor.put("city", "9400 Rorschach");
		debtor.put("country", "CH");
		Object result = new SwissQRBillGenerator().generate(node.toString());
		assertEquals(String.class, result.getClass());
		System.out.println(result.toString());
		mapper = new ObjectMapper();
		JsonNode resultNode = mapper.readTree(result.toString());
		assertEquals(1, resultNode.size());
		assertEquals(JsonNodeType.ARRAY, resultNode.getNodeType());
		assertEquals("'output_size' must be set with one of A4_PORTRAIT_SHEET, QR_BILL_ONLY, QR_BILL_WITH_HORIZONTAL_LINE, QR_CODE_ONLY, QR_BILL_EXTRA_SPACE", resultNode.get(0).get("illegal_argument_exception").asText());
	}
	
	@Test
	public void testInvalidOutputSize() throws JsonMappingException, JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("output", output);
		node.put("graphics_format", GraphicsFormat.PDF.name());
		node.put("output_size", "QRB");
		node.put("language", Language.DE.name());
		node.put("iban", "CH4431999123000889012");
		node.put("amount", 199.95);
		node.put("currency", "CHF");
		ObjectNode creditor = node.putObject("creditor");
		creditor.put("name", "Robert Schneider AG");
		creditor.put("address", "Rue du Lac 1268/2/22");
		creditor.put("city", "2501 Biel");
		creditor.put("country", "CH");
		node.put("reference", "210000000003139471430009017");
		node.put("message", "Abonnement für 2020");
		ObjectNode debtor = node.putObject("debtor");
		debtor.put("name", "Pia-Maria Rutschmann-Schnyder");
		debtor.put("address", "Grosse Marktgasse 28");
		debtor.put("city", "9400 Rorschach");
		debtor.put("country", "CH");
		Object result = new SwissQRBillGenerator().generate(node.toString());
		assertEquals(String.class, result.getClass());
		System.out.println(result.toString());
		mapper = new ObjectMapper();
		JsonNode resultNode = mapper.readTree(result.toString());
		assertEquals(1, resultNode.size());
		assertEquals(JsonNodeType.ARRAY, resultNode.getNodeType());
		assertEquals("'output_size' must be set with one of A4_PORTRAIT_SHEET, QR_BILL_ONLY, QR_BILL_WITH_HORIZONTAL_LINE, QR_CODE_ONLY, QR_BILL_EXTRA_SPACE", resultNode.get(0).get("illegal_argument_exception").asText());
	}
	
	@Test
	public void testMissingOutput() throws JsonMappingException, JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("output_size", OutputSize.QR_CODE_ONLY.name());
		node.put("graphics_format", GraphicsFormat.PDF.name());
		node.put("language", Language.DE.name());
		node.put("iban", "CH4431999123000889012");
		node.put("amount", 199.95);
		node.put("currency", "CHF");
		ObjectNode creditor = node.putObject("creditor");
		creditor.put("name", "Robert Schneider AG");
		creditor.put("address", "Rue du Lac 1268/2/22");
		creditor.put("city", "2501 Biel");
		creditor.put("country", "CH");
		node.put("reference", "210000000003139471430009017");
		node.put("message", "Abonnement für 2020");
		ObjectNode debtor = node.putObject("debtor");
		debtor.put("name", "Pia-Maria Rutschmann-Schnyder");
		debtor.put("address", "Grosse Marktgasse 28");
		debtor.put("city", "9400 Rorschach");
		debtor.put("country", "CH");
		Object result = new SwissQRBillGenerator().generate(node.toString());
		assertEquals(String.class, result.getClass());
		System.out.println(result.toString());
		mapper = new ObjectMapper();
		JsonNode resultNode = mapper.readTree(result.toString());
		assertEquals(1, resultNode.size());
		assertEquals(JsonNodeType.ARRAY, resultNode.getNodeType());
		assertEquals("'output' must be a valid file pathname", resultNode.get(0).get("illegal_argument_exception").asText());
	}
	
	@Test
	public void testInvalidOutput() throws JsonMappingException, JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("graphics_format", GraphicsFormat.PDF.name());
		node.put("output_size", OutputSize.A4_PORTRAIT_SHEET.name());
		node.put("output", "");
		node.put("language", Language.DE.name());
		node.put("iban", "CH4431999123000889012");
		node.put("amount", 199.95);
		node.put("currency", "CHF");
		ObjectNode creditor = node.putObject("creditor");
		creditor.put("name", "Robert Schneider AG");
		creditor.put("address", "Rue du Lac 1268/2/22");
		creditor.put("city", "2501 Biel");
		creditor.put("country", "CH");
		node.put("reference", "210000000003139471430009017");
		node.put("message", "Abonnement für 2020");
		ObjectNode debtor = node.putObject("debtor");
		debtor.put("name", "Pia-Maria Rutschmann-Schnyder");
		debtor.put("address", "Grosse Marktgasse 28");
		debtor.put("city", "9400 Rorschach");
		debtor.put("country", "CH");
		Object result = new SwissQRBillGenerator().generate(node.toString());
		assertEquals(String.class, result.getClass());
		System.out.println(result.toString());
		mapper = new ObjectMapper();
		JsonNode resultNode = mapper.readTree(result.toString());
		assertEquals(1, resultNode.size());
		assertEquals(JsonNodeType.ARRAY, resultNode.getNodeType());
		assertEquals("No such file or directory", resultNode.get(0).get("io_exception").asText());
	}
	
	@Test
	public void testMissingGraphicsFormat() throws JsonMappingException, JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("output", output);
		node.put("output_size", OutputSize.QR_CODE_ONLY.name());
		node.put("language", Language.DE.name());
		node.put("iban", "CH4431999123000889012");
		node.put("amount", 199.95);
		node.put("currency", "CHF");
		ObjectNode creditor = node.putObject("creditor");
		creditor.put("name", "Robert Schneider AG");
		creditor.put("address", "Rue du Lac 1268/2/22");
		creditor.put("city", "2501 Biel");
		creditor.put("country", "CH");
		node.put("reference", "210000000003139471430009017");
		node.put("message", "Abonnement für 2020");
		ObjectNode debtor = node.putObject("debtor");
		debtor.put("name", "Pia-Maria Rutschmann-Schnyder");
		debtor.put("address", "Grosse Marktgasse 28");
		debtor.put("city", "9400 Rorschach");
		debtor.put("country", "CH");
		Object result = new SwissQRBillGenerator().generate(node.toString());
		assertEquals(String.class, result.getClass());
		System.out.println(result.toString());
		mapper = new ObjectMapper();
		JsonNode resultNode = mapper.readTree(result.toString());
		assertEquals(1, resultNode.size());
		assertEquals(JsonNodeType.ARRAY, resultNode.getNodeType());
		assertEquals("'graphics_format' must be set with one of PDF, SVG, PNG", resultNode.get(0).get("illegal_argument_exception").asText());
	}
	
	@Test
	public void testInvalidGraphicsFormat() throws JsonMappingException, JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("output", output);
		node.put("output_size", OutputSize.QR_CODE_ONLY.name());
		node.put("graphics_format", "QRB");
		node.put("language", Language.DE.name());
		node.put("iban", "CH4431999123000889012");
		node.put("amount", 199.95);
		node.put("currency", "CHF");
		ObjectNode creditor = node.putObject("creditor");
		creditor.put("name", "Robert Schneider AG");
		creditor.put("address", "Rue du Lac 1268/2/22");
		creditor.put("city", "2501 Biel");
		creditor.put("country", "CH");
		node.put("reference", "210000000003139471430009017");
		node.put("message", "Abonnement für 2020");
		ObjectNode debtor = node.putObject("debtor");
		debtor.put("name", "Pia-Maria Rutschmann-Schnyder");
		debtor.put("address", "Grosse Marktgasse 28");
		debtor.put("city", "9400 Rorschach");
		debtor.put("country", "CH");
		Object result = new SwissQRBillGenerator().generate(node.toString());
		assertEquals(String.class, result.getClass());
		System.out.println(result.toString());
		mapper = new ObjectMapper();
		JsonNode resultNode = mapper.readTree(result.toString());
		assertEquals(1, resultNode.size());
		assertEquals(JsonNodeType.ARRAY, resultNode.getNodeType());
		assertEquals("'graphics_format' must be set with one of PDF, SVG, PNG", resultNode.get(0).get("illegal_argument_exception").asText());
	}
	
	@Test
	public void testWithOriginalJsonStringFromFileMakerForQRCodeOnly()
	{
		String param = "{\"output\":\"" + output + "\", \"graphics_format\" : \"PDF\", \"output_size\" : \"QR_BILL_ONLY\", \"amount\":199.95,\"creditor\":{\"address\":\"Rue du Lac 1268/2/22\",\"city\":\"2501 Biel\",\"country\":\"CH\",\"name\":\"Robert Schneider AG\"},\"currency\":\"CHF\",\"debtor\":{\"address\":\"Grosse Marktgasse 28\",\"city\":\"9400 Rorschach\",\"country\":\"CH\",\"name\":\"Pia-Maria Rutschmann-Schnyder\"},\"iban\":\"CH4431999123000889012\",\"message\":\"Abonnement für 2020\",\"reference\":\"210000000003139471430009017\"}";
		Object result = new SwissQRBillGenerator().generate(param);
		assertEquals("OK", result);	
	}

	@Test
	public void testWithOriginalJsonStringFromFileMakerForQRBill()
	{
		String param = "{\"output\":\"" + output + "\", \"graphics_format\" : \"PDF\", \"output_size\" : \"QR_BILL_ONLY\", \"amount\":199.95,\"creditor\":{\"address\":\"Rue du Lac 1268/2/22\",\"city\":\"2501 Biel\",\"country\":\"CH\",\"name\":\"Robert Schneider AG\"},\"currency\":\"CHF\",\"debtor\":{\"address\":\"Grosse Marktgasse 28\",\"city\":\"9400 Rorschach\",\"country\":\"CH\",\"name\":\"Pia-Maria Rutschmann-Schnyder\"},\"iban\":\"CH4431999123000889012\",\"message\":\"Abonnement für 2020\",\"reference\":\"210000000003139471430009017\"}";
		Object result = new SwissQRBillGenerator().generate(param);
		assertEquals("OK", result);	
	}

	@Test
	public void testWithNullParameter() throws JsonMappingException, JsonProcessingException
	{
		Object result = new SwissQRBillGenerator().generate(null);
		assertEquals(String.class, result.getClass());	
		ObjectMapper mapper = new ObjectMapper();
		JsonNode resultNode = mapper.readTree(result.toString());
		assertEquals(1, resultNode.size());
		assertEquals(JsonNodeType.ARRAY, resultNode.getNodeType());
		assertEquals("argument \"content\" is null", resultNode.get(0).get("illegal_argument_exception").asText());
	}

	@Test
	public void testWithQRIban() throws JsonMappingException, JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("output", output);
		node.put("graphics_format", GraphicsFormat.PDF.name());
		node.put("output_size", OutputSize.QR_BILL_ONLY.name());
		node.put("language", Language.DE.name());
		node.put("iban", "CH4431999123000889012");
		node.put("amount", 199.95);
		node.put("currency", "CHF");
		ObjectNode creditor = node.putObject("creditor");
		creditor.put("name", "Robert Schneider AG");
		creditor.put("address", "Rue du Lac 1268/2/22");
		creditor.put("city", "2501 Biel");
		creditor.put("country", "CH");
		node.put("reference", "210000000003139471430009017");
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
	public void testWithoutReference() throws JsonMappingException, JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("output", output);
		node.put("graphics_format", GraphicsFormat.PDF.name());
		node.put("output_size", OutputSize.QR_BILL_ONLY.name());
		node.put("language", Language.DE.name());
		node.put("iban", "CH6309000000901197203");
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
	public void testWithoutAmount() throws JsonMappingException, JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("output", output);
		node.put("graphics_format", GraphicsFormat.PDF.name());
		node.put("output_size", OutputSize.QR_BILL_ONLY.name());
		node.put("language", Language.DE.name());
		node.put("iban", "CH6309000000901197203");
		node.put("currency", "CHF");
		ObjectNode creditor = node.putObject("creditor");
		creditor.put("name", "Robert Schneider AG");
		creditor.put("address", "Rue du Lac 1268/2/22");
		creditor.put("city", "2501 Biel");
		creditor.put("country", "CH");
//		node.put("reference", "210000000003139471430009017");
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
	public void testWithNullAmount() throws JsonMappingException, JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("output", output);
		node.put("graphics_format", GraphicsFormat.PDF.name());
		node.put("output_size", OutputSize.QR_BILL_ONLY.name());
		node.put("language", Language.DE.name());
		node.put("iban", "CH6309000000901197203");
		node.putNull("amount");
		node.put("currency", "CHF");
		ObjectNode creditor = node.putObject("creditor");
		creditor.put("name", "Robert Schneider AG");
		creditor.put("address", "Rue du Lac 1268/2/22");
		creditor.put("city", "2501 Biel");
		creditor.put("country", "CH");
		node.put("reference", "");
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
	public void testWith0Amount() throws JsonMappingException, JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("output", output);
		node.put("graphics_format", GraphicsFormat.PDF.name());
		node.put("output_size", OutputSize.QR_BILL_ONLY.name());
		node.put("language", Language.DE.name());
		node.put("iban", "CH6309000000901197203");
		node.put("amount", 0);
		node.put("currency", "CHF");
		ObjectNode creditor = node.putObject("creditor");
		creditor.put("name", "Robert Schneider AG");
		creditor.put("address", "Rue du Lac 1268/2/22");
		creditor.put("city", "2501 Biel");
		creditor.put("country", "CH");
		node.put("reference", "");
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
	public void testWithoutDebtor() throws JsonMappingException, JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();
		node.put("output", output);
		node.put("graphics_format", GraphicsFormat.PDF.name());
		node.put("output_size", OutputSize.QR_BILL_ONLY.name());
		node.put("language", Language.DE.name());
		node.put("iban", "CH6309000000901197203");
		node.put("amount", 199.95);
		node.put("currency", "CHF");
		ObjectNode creditor = node.putObject("creditor");
		creditor.put("name", "Robert Schneider AG");
		creditor.put("address", "Rue du Lac 1268/2/22");
		creditor.put("city", "2501 Biel");
		creditor.put("country", "CH");
		node.put("message", "Abonnement für 2020");
		Object result = new SwissQRBillGenerator().generate(node.toString());
		assertEquals("OK", result);	
	}
}
