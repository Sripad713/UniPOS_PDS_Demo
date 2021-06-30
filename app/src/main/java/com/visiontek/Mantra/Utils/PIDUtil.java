package com.visiontek.Mantra.Utils;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class PIDUtil {
    public static String getMantraPIDXml() {
        String tmpOptXml = "";
        try{
            String fTypeStr = "0";
            String formatStr = "0";
            String timeOutStr = "20000";
            String envStr = "P";

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = null;

            docBuilder = docFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = docBuilder.newDocument();
            doc.setXmlStandalone(true);

            Element rootElement = doc.createElement("PidOptions");
            Attr ver = doc.createAttribute("ver");
            ver.setValue("1.0");
            rootElement.setAttributeNode(ver);
            doc.appendChild(rootElement);

            Element opts = doc.createElement("Opts");
            rootElement.appendChild(opts);

            Attr attr = doc.createAttribute("fCount");
            attr.setValue(String.valueOf(1));
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("fType");
            attr.setValue(fTypeStr);
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("iCount");
            attr.setValue("0");
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("iType");
            attr.setValue("0");
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("pCount");
            attr.setValue("0");
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("pType");
            attr.setValue("0");
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("format");
            attr.setValue(formatStr);
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("pidVer");
            attr.setValue("2.0");
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("timeout");
            attr.setValue(timeOutStr);
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("otp");
            attr.setValue("");
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("env");
            attr.setValue(envStr);
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("wadh");
            attr.setValue("");
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("posh");
            attr.setValue("UNKNOWN");
            opts.setAttributeNode(attr);

            Element demo = doc.createElement("Demo");
            demo.setTextContent("");
            rootElement.appendChild(demo);

            Element custotp = doc.createElement("CustOpts");
            rootElement.appendChild(custotp);

            Element param = doc.createElement("Param");
            custotp.appendChild(param);

            attr = doc.createAttribute("name");
            attr.setValue("ValidationKey");
            param.setAttributeNode(attr);

            attr = doc.createAttribute("value");
            attr.setValue("ONLY USE FOR LOCKED DEVICES.");
            param.setAttributeNode(attr);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            DOMSource source = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);

            tmpOptXml = writer.getBuffer().toString().replaceAll("\n|\r", "");
            tmpOptXml = tmpOptXml.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
            //System.out.println(tmpOptXml);
            return tmpOptXml;
        }catch(Exception ex){
            ex.printStackTrace();
            return "";
        }
    }
    public static String getOthersXml() {
        String tmpOptXml = "";
        try{
            String fTypeStr = "0";
            String formatStr = "0";
            String timeOutStr = "20000";
            String envStr = "P";

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = null;

            docBuilder = docFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = docBuilder.newDocument();
            doc.setXmlStandalone(true);

            Element rootElement = doc.createElement("PidOptions");
            doc.appendChild(rootElement);

            Element opts = doc.createElement("Opts");
            rootElement.appendChild(opts);

            Attr attr = doc.createAttribute("fCount");
            attr.setValue(String.valueOf(1));
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("fType");
            attr.setValue(fTypeStr);
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("iCount");
            attr.setValue("0");
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("iType");
            attr.setValue("");
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("pCount");
            attr.setValue("0");
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("pType");
            attr.setValue("");
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("format");
            attr.setValue(formatStr);
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("pidVer");
            attr.setValue("2.0");
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("timeout");
            attr.setValue(timeOutStr);
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("otp");
            attr.setValue("");
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("env");
            attr.setValue(envStr);
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("wadh");
            attr.setValue("");
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("posh");
            attr.setValue("UNKNOWN");
            opts.setAttributeNode(attr);

            Element demo = doc.createElement("Demo");
            demo.setTextContent("");
            rootElement.appendChild(demo);

            Element custotp = doc.createElement("CustOpts");
            rootElement.appendChild(custotp);

            Element param = doc.createElement("Param");
            custotp.appendChild(param);

            attr = doc.createAttribute("name");
            attr.setValue("ValidationKey");
            param.setAttributeNode(attr);

            attr = doc.createAttribute("value");
            attr.setValue("ONLY USE FOR LOCKED DEVICES.");
            param.setAttributeNode(attr);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            DOMSource source = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);

            tmpOptXml = writer.getBuffer().toString().replaceAll("\n|\r", "");
            tmpOptXml = tmpOptXml.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
            //System.out.println(tmpOptXml);
            return tmpOptXml;
        }catch(Exception ex){
            ex.printStackTrace();
            return "";
        }
    }

    public static String getOthersEkycXml() {
        String tmpOptXml = "";
        try {
            String fTypeStr = "0";
            String formatStr = "0";
            String timeOutStr = "20000";
            String envStr = "P";


            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = null;

            docBuilder = docFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = docBuilder.newDocument();
            doc.setXmlStandalone(true);

            Element rootElement = doc.createElement("PidOptions");
            doc.appendChild(rootElement);

            Element opts = doc.createElement("Opts");
            rootElement.appendChild(opts);

            Attr attr = doc.createAttribute("fCount");
            attr.setValue(String.valueOf(1));
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("fType");
            attr.setValue(fTypeStr);
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("iCount");
            attr.setValue("0");
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("iType");
            attr.setValue("");
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("pCount");
            attr.setValue("0");
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("pType");
            attr.setValue("");
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("format");
            attr.setValue(formatStr);
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("pidVer");
            attr.setValue("2.0");
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("timeout");
            attr.setValue(timeOutStr);
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("otp");
            attr.setValue("");
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("env");
            attr.setValue(envStr);
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("wadh");
            attr.setValue("RZ+k4w9ySTzOibQdDHPzCFqrKScZ74b3EibKYy1WyGw=");
            opts.setAttributeNode(attr);

            attr = doc.createAttribute("posh");
            attr.setValue("UNKNOWN");
            opts.setAttributeNode(attr);

            Element demo = doc.createElement("Demo");
            demo.setTextContent("");
            rootElement.appendChild(demo);

            Element custotp = doc.createElement("CustOpts");
            rootElement.appendChild(custotp);

            Element param = doc.createElement("Param");
            custotp.appendChild(param);


            attr = doc.createAttribute("name");
            attr.setValue("ValidationKey");
            param.setAttributeNode(attr);

            attr = doc.createAttribute("value");
            attr.setValue("ONLY USE FOR LOCKED DEVICES.");
            param.setAttributeNode(attr);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            DOMSource source = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);

            tmpOptXml = writer.getBuffer().toString().replaceAll("\n|\r", "");
            tmpOptXml = tmpOptXml.replaceAll("&lt;", "<").replaceAll("&gt;", ">");

            return tmpOptXml;
        } catch (Exception ex) {
            return "";
        }
    }
        public static String getMantraEkycXml() {
            String tmpOptXml = "";
            try {
                String fTypeStr = "0";
                String formatStr = "0";
                String timeOutStr = "20000";
                String envStr = "P";

                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                docFactory.setNamespaceAware(true);
                DocumentBuilder docBuilder = null;

                docBuilder = docFactory.newDocumentBuilder();
                org.w3c.dom.Document doc = docBuilder.newDocument();
                doc.setXmlStandalone(true);

                Element rootElement = doc.createElement("PidOptions");
                Attr ver = doc.createAttribute("ver");
                ver.setValue("1.0");
                rootElement.setAttributeNode(ver);
                doc.appendChild(rootElement);

                Element opts = doc.createElement("Opts");
                rootElement.appendChild(opts);

                Attr attr = doc.createAttribute("fCount");
                attr.setValue(String.valueOf(1));
                opts.setAttributeNode(attr);

                attr = doc.createAttribute("fType");
                attr.setValue(fTypeStr);
                opts.setAttributeNode(attr);

                attr = doc.createAttribute("iCount");
                attr.setValue("0");
                opts.setAttributeNode(attr);

                attr = doc.createAttribute("iType");
                attr.setValue("0");
                opts.setAttributeNode(attr);

                attr = doc.createAttribute("pCount");
                attr.setValue("0");
                opts.setAttributeNode(attr);

                attr = doc.createAttribute("pType");
                attr.setValue("0");
                opts.setAttributeNode(attr);

                attr = doc.createAttribute("format");
                attr.setValue(formatStr);
                opts.setAttributeNode(attr);

                attr = doc.createAttribute("pidVer");
                attr.setValue("2.0");
                opts.setAttributeNode(attr);

                attr = doc.createAttribute("timeout");
                attr.setValue(timeOutStr);
                opts.setAttributeNode(attr);

                attr = doc.createAttribute("otp");
                attr.setValue("");
                opts.setAttributeNode(attr);

                attr = doc.createAttribute("env");
                attr.setValue(envStr);
                opts.setAttributeNode(attr);

                attr = doc.createAttribute("wadh");
                attr.setValue("RZ+k4w9ySTzOibQdDHPzCFqrKScZ74b3EibKYy1WyGw=");
                opts.setAttributeNode(attr);

                attr = doc.createAttribute("posh");
                attr.setValue("UNKNOWN");
                opts.setAttributeNode(attr);

                Element demo = doc.createElement("Demo");
                demo.setTextContent("");
                rootElement.appendChild(demo);

                Element custotp = doc.createElement("CustOpts");
                rootElement.appendChild(custotp);

                Element param = doc.createElement("Param");
                custotp.appendChild(param);


                attr = doc.createAttribute("name");
                attr.setValue("ValidationKey");
                param.setAttributeNode(attr);

                attr = doc.createAttribute("value");
                attr.setValue("ONLY USE FOR LOCKED DEVICES.");
                param.setAttributeNode(attr);

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
                DOMSource source = new DOMSource(doc);
                StringWriter writer = new StringWriter();
                StreamResult result = new StreamResult(writer);
                transformer.transform(source, result);

                tmpOptXml = writer.getBuffer().toString().replaceAll("\n|\r", "");
                tmpOptXml = tmpOptXml.replaceAll("&lt;", "<").replaceAll("&gt;", ">");

                return tmpOptXml;
            } catch (Exception ex) {
                return "";
            }
    }
}
