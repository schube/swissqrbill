package ch.eugster.swissqrcode;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
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
				try
				{
					URI uri = new URI(node.get("path").get("output").asText());
					output = Paths.get(uri);
				}
				catch (URISyntaxException e)
				{
					output = Paths.get(node.get("path").get("output").asText());
				}
				catch (FileSystemNotFoundException e)
				{
					output = Paths.get(node.get("path").get("output").asText());
				}
			}
			catch (Exception e)
			{
				ObjectNode msg = mapper.createObjectNode();
				IllegalArgumentException iae = new IllegalArgumentException("'output' must be a valid URI");
				msg.put("illegal_argument_exception", iae.getMessage());
				result.add(msg);
			} 

			Bill bill = new Bill();
			try
			{
				BillFormat format = null;
				try
				{
					format = new BillFormat();
					format.setFontFamily("Arial");
					format.setGraphicsFormat(selectGraphicsFormat(node.get("form")));
					format.setLanguage(guessLanguage(node.get("form")));
					format.setOutputSize(selectOutputSize(node.get("form")));
				}
				catch (IllegalArgumentException e)
				{
					ObjectNode msg = mapper.createObjectNode();
					msg.put("illegal_argument_exception", e.getMessage());
					result.add(msg);
				}
				
	
				// Setup bill
				bill.setFormat(format);
				JsonNode amount = node.get("amount");
				bill.setAmountFromDouble(amount.equals(Double.valueOf(0L)) ? null : node.get("amount").asDouble());
				bill.setCurrency(node.get("currency").asText());
				bill.setReferenceType(Bill.REFERENCE_TYPE_NO_REF);
		
				// Set creditor
				Address creditor = new Address();
				creditor.setName(node.get("creditor").get("name").asText());
				creditor.setAddressLine1(node.get("creditor").get("address").asText());
				creditor.setAddressLine2(node.get("creditor").get("city").asText());
				creditor.setCountryCode(node.get("creditor").get("country").asText());
				bill.setCreditor(creditor);
				bill.setAccount(node.get("iban").asText());
		
				// more bill data
				StringBuilder reference = new StringBuilder();
				if (Objects.isNull(node.get("reference")))
				{
					if (node.get("invoice") != null)
					{
						try
						{
							reference = reference.append(new BigInteger(node.get("invoice").asText()).toString());
						}
						catch (NumberFormatException e)
						{
							// Do nothing
						}
					}
					if (node.get("debtor").get("number") != null)
					{
						try
						{
							reference = reference.append(new BigInteger(node.get("debtor").get("number").asText()).toString());
						}
						catch (NumberFormatException e)
						{
							// Do nothing
						}
					}
				}
				else
				{
					try
					{
						reference = reference.append(new BigInteger(node.get("reference").asText()).toString());
					}
					catch (NumberFormatException e)
					{
						// Do nothing: reference is already initialized with ""
					}
				}
				if (reference.length() == 27)
				{
					bill.setReference(reference.toString());
				}
				else
				{
					bill.createAndSetQRReference(reference.toString());
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
			}
			catch (Exception e)
			{
				System.out.println();
			}
	
			// Validate QR bill
			ValidationResult validation = QRBill.validate(bill);
			if (validation.isValid() && result.isEmpty())
			{
				Path invoice = null;
				if (node.get("path").get("invoice") != null && node.get("path").get("invoice").asText() != null)
				{
					try
					{
						URI uri = new URI(node.get("path").get("invoice").asText());
						invoice = Paths.get(uri);
					}
					catch (URISyntaxException e)
					{
						invoice = Paths.get(node.get("path").get("invoice").asText());
					}
					catch (FileSystemNotFoundException e)
					{
						invoice = Paths.get(node.get("path").get("invoice").asText());
					}
				}
				
				if (!Objects.isNull(invoice))
				{
					try
					{
						PDFCanvas canvas = new PDFCanvas(invoice, PDFCanvas.LAST_PAGE);
						QRBill.draw(bill, canvas);
						canvas.saveAs(output);
						return "OK";
					}
					catch (IOException e)
					{
						ObjectNode msg = mapper.createObjectNode();
						msg.put("io_exception", "Source path '" + e.getMessage() + "' does not exist");
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
		OutputSize outputSize = null;
		JsonNode size = node.get("output_size");
		if (size == null || size.asText() == null || size.asText().trim().isEmpty())
		{
			if (node.get("path").get("invoice") == null)
			{
				outputSize = OutputSize.A4_PORTRAIT_SHEET;
			}
			else
			{
				outputSize = OutputSize.QR_BILL_EXTRA_SPACE;
			}
		}
		try
		{
			outputSize = OutputSize.valueOf(size.asText());
		}
		catch (IllegalArgumentException e)
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