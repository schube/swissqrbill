package ch.eugster.swissqrcode;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.codecrete.qrbill.canvas.PDFCanvas;
import net.codecrete.qrbill.generator.Address;
import net.codecrete.qrbill.generator.Bill;
import net.codecrete.qrbill.generator.BillFormat;
import net.codecrete.qrbill.generator.GraphicsFormat;
import net.codecrete.qrbill.generator.Language;
import net.codecrete.qrbill.generator.OutputSize;
import net.codecrete.qrbill.generator.QRBill;
import net.codecrete.qrbill.generator.ValidationMessage;
import net.codecrete.qrbill.generator.ValidationResult;

public class SwissQRBillGenerator 
{
	public Object generate(String json) 
	{
		// convert JSON string to Map
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode result = mapper.createArrayNode();
		JsonNode node = null;
		try 
		{
			node = mapper.readTree(json);
			if (node == null)
			{
				ObjectNode msg = mapper.createObjectNode();
				IllegalArgumentException iae = new IllegalArgumentException("Missing parameter. Yout must provide a valid json parameter");
				msg.put("illegal_argument_exception", iae.getMessage());
				result.add(msg);
			}
		} 
		catch (IllegalArgumentException e) 
		{
			ObjectNode msg = mapper.createObjectNode();
			msg.put("illegal_argument_exception", e.getMessage());
			result.add(msg);
		} 
		catch (JsonMappingException e) 
		{
			ObjectNode msg = mapper.createObjectNode();
			msg.put("json_mapping_exception", e.getMessage());
			result.add(msg);
		} 
		catch (JsonProcessingException e) 
		{
			ObjectNode msg= mapper.createObjectNode();
			msg.put("json_processing_exception", e.getMessage());
			result.add(msg);
		}

		if (node != null)
		{
			Path output = null;
			try
			{
				URI uri = new URI(node.get("output").asText());
				output = Paths.get(uri);
			}
			catch (Exception e)
			{
				ObjectNode msg = mapper.createObjectNode();
				IllegalArgumentException iae = new IllegalArgumentException("'output' must be a valid URI");
				msg.put("illegal_argument_exception", iae.getMessage());
				result.add(msg);
			} 

			BillFormat format = null;
			try
			{
				format = new BillFormat();
				format.setFontFamily("Arial");
				format.setGraphicsFormat(selectGraphicsFormat(node));
				format.setLanguage(guessLanguage(node));
				format.setOutputSize(selectOutputSize(node));
			}
			catch (IllegalArgumentException e)
			{
				ObjectNode msg = mapper.createObjectNode();
				msg.put("illegal_argument_exception", e.getMessage());
				result.add(msg);
			}
			
	
			// Setup bill
			Bill bill = new Bill();
			bill.setFormat(format);
			bill.setAccount(node.get("iban").asText());
			JsonNode amount = node.get("amount");
			bill.setAmountFromDouble(amount == null ? null : node.get("amount").asDouble());
			bill.setCurrency(node.get("currency").asText());
	//		bill.setReferenceType(Bill.REFERENCE_TYPE_NO_REF);
	
			// Set creditor
			Address creditor = new Address();
			creditor.setName(node.get("creditor").get("name").asText());
			creditor.setAddressLine1(node.get("creditor").get("address").asText());
			creditor.setAddressLine2(node.get("creditor").get("city").asText());
			creditor.setCountryCode(node.get("creditor").get("country").asText());
			bill.setCreditor(creditor);
	
			// more bill data
			String ref = null;
			JsonNode reference = node.get("reference");
			if (!Objects.isNull(reference))
			{
				if (!reference.asText().trim().isEmpty())
				{
					ref = reference.asText();
				}
			}
			try
			{
				bill.setReference(ref);
			}
			catch (IllegalArgumentException e)
			{
				bill.createAndSetQRReference(ref);
			}
			bill.setUnstructuredMessage(node.get("message").asText());
	
			// Set debtor
			JsonNode debtor = node.get("debtor");
			if (!Objects.isNull(debtor))
			{
				Address address = new Address();
				address.setName(debtor.get("name").asText());
				address.setAddressLine1(debtor.get("address").asText());
				address.setAddressLine2(debtor.get("city").asText());
				address.setCountryCode(debtor.get("country").asText());
				bill.setDebtor(address);
			}
	
			// Validate QR bill
			ValidationResult validation = QRBill.validate(bill);
			if (validation.isValid() && result.isEmpty())
			{
				URI invoice = null;
				try
				{
					if (node.get("invoice") != null && node.get("invoice").asText() != null)
					{
						invoice = new URI(node.get("invoice").asText());
					}
				}
				catch (URISyntaxException e)
				{
					ObjectNode msg = mapper.createObjectNode();
					IllegalArgumentException iae = new IllegalArgumentException("'invoice' must be a valid URI");
					msg.put("uri_syntax_exception", iae.getMessage());
					result.add(msg);
				}
				
				if (!Objects.isNull(invoice))
				{
					Path invoiceWithoutQRBill = Paths.get(invoice);
					try
					{
						PDFCanvas canvas = new PDFCanvas(invoiceWithoutQRBill, PDFCanvas.LAST_PAGE);
						QRBill.draw(bill, canvas);
						canvas.saveAs(output);
						return "OK";
					}
					catch (IOException e)
					{
						ObjectNode msg = mapper.createObjectNode();
						msg.put("io_exception", "Creation of output file '" + output.getFileName() + "' failed.");
						result.add(msg);
					}
				}
				else
				{
					// Generate QR bill
					byte[] bytes = QRBill.generate(bill);
					try 
					{
						if (output.toFile().exists())
						{
							output.toFile().delete();
						}
						if (output.toFile().createNewFile())
						{
							OutputStream os = new FileOutputStream(output.toFile());
							os.write(bytes);
							os.close();
							return "OK";
						}
					} 
					catch (FileNotFoundException e) 
					{
						ObjectNode msg = mapper.createObjectNode();
						msg.put("file_not_found_exception", e.getMessage());
						result.add(msg);
					} 
					catch (IOException e) 
					{
						ObjectNode msg = mapper.createObjectNode();
						msg.put("io_exception", e.getMessage());
						result.add(msg);
					}
				}
			}
			else
			{
				List<ValidationMessage> messages = validation.getValidationMessages();
				for (ValidationMessage message : messages)
				{
					ObjectNode msg = mapper.createObjectNode();
					msg.put(message.getField(), message.getMessageKey());
					result.add(msg);
				}
			}
		}
		return result.toString();
	}
	
	private Language guessLanguage(JsonNode node)
	{
		JsonNode languageNode = node.get("language");
		if (languageNode != null && languageNode.asText() != null && !languageNode.asText().trim().isEmpty())
		{
			String[] availableLanguages = new String[] { "EN", "DE", "FR", "IT" };
			for (String availableLanguage : availableLanguages)
			{
				if (availableLanguage.equals(languageNode.asText()))
				{
					return Language.valueOf(availableLanguage);
				}
			}
		}
		if (Locale.getDefault().getLanguage().equals(Locale.GERMAN.getLanguage()))
		{
			return Language.DE;
		}
		else if (Locale.getDefault().getLanguage().equals(Locale.FRENCH.getLanguage()))
		{
			return Language.FR;
		}
		else if (Locale.getDefault().getLanguage().equals(Locale.ITALIAN.getLanguage()))
		{
			return Language.IT;
		}
		else
		{
			return Language.EN;
		}
	}
	
	private OutputSize selectOutputSize(JsonNode node) throws IllegalArgumentException
	{
		JsonNode outputType = node.get("output_size");
		if (outputType == null || outputType.asText() == null || outputType.asText().trim().isEmpty())
		{
			throw new IllegalArgumentException(buildOutputSizeErrorMessage());
		}
		OutputSize outputSize = null;
		try
		{
			outputSize = OutputSize.valueOf(outputType.asText());
		}
		catch (IllegalArgumentException e)
		{
			throw new IllegalArgumentException(buildOutputSizeErrorMessage());
		}
		if (outputSize == null || outputSize.getClass().isAnnotationPresent(Deprecated.class))
		{
			throw new IllegalArgumentException(buildOutputSizeErrorMessage());
		}
		return outputSize;
	}

	private GraphicsFormat selectGraphicsFormat(JsonNode node) throws IllegalArgumentException
	{
		JsonNode graphicsFormat = node.get("graphics_format");
		if (graphicsFormat == null || graphicsFormat.asText() == null || graphicsFormat.asText().trim().isEmpty())
		{
			throw new IllegalArgumentException(buildGraphicsFormatErrorMessage());
		}
		GraphicsFormat format = null;
		try
		{
			format = GraphicsFormat.valueOf(graphicsFormat.asText());
		}
		catch (IllegalArgumentException e)
		{
			throw new IllegalArgumentException(buildGraphicsFormatErrorMessage());
		}
		if (format == null || format.getClass().isAnnotationPresent(Deprecated.class))
		{
			throw new IllegalArgumentException(buildGraphicsFormatErrorMessage());
		}
		return format;
	}
	
	private String buildOutputSizeErrorMessage()
	{
		StringBuilder builder = new StringBuilder();
		for (OutputSize size : OutputSize.values())
		{
			builder = builder.append(size.name() + ", ");
		}
		String values = builder.toString().trim();
		values = values.substring(0, values.length() - 1);
		return "'output_size' must be set with one of " + values;
	}
	
	private String buildGraphicsFormatErrorMessage()
	{
		StringBuilder builder = new StringBuilder();
		for (GraphicsFormat format : GraphicsFormat.values())
		{
			builder = builder.append(format.name() + ", ");
		}
		String values = builder.toString().trim();
		values = values.substring(0, values.length() - 1);
		return "'graphics_format' must be set with one of " + values;
	}
}