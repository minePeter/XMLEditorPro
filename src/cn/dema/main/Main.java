package cn.dema.main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {
	
	private final static String CMD_TXT = "[usage java -jar AXMLEditor.jar [-tag|-attr] [-i|-r|-m] ...";

	public static void main(String[] args){
		if(args.length < 3){
			System.out.println(CMD_TXT);
			return;
		}
		
		String inputfile = args[args.length-2];
		String outputfile = args[args.length-1];
		File inputFile = new File(inputfile);
		File outputFile = new File(outputfile);
		if(!inputFile.exists()){
			System.out.println(CMD_TXT);
			return;
		}

		FileInputStream fis = null;
		ByteArrayOutputStream bos = null;
		try{
			fis = new FileInputStream(inputFile);
			bos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = 0;
			while((len=fis.read(buffer)) != -1){
				bos.write(buffer, 0, len);
			}
			ParserChunkUtils.xmlStruct.byteSrc = bos.toByteArray();
		}catch(Exception e){
			System.out.println(CMD_TXT);
		}finally{
			try{
				fis.close();
				bos.close();
			}catch(Exception e){
			}
		}
		
		doCommand(args);

		if(!outputFile.exists()){
			outputFile.delete();
		}
		FileOutputStream fos = null;
		try{
			fos = new FileOutputStream(outputFile);
			fos.write(ParserChunkUtils.xmlStruct.byteSrc);
			fos.close();
		}catch(Exception e){
		}finally{
			if(fos != null){
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static void testDemo(){
	}

	public static void doCommand(String[] args){
		if("-tag".equals(args[0])){
			if(args.length < 2){
				System.out.println(CMD_TXT);
				return;
			}
			if("-i".equals(args[1])){
				if(args.length < 3){
					System.out.println(CMD_TXT);
					return;
				}
				String insertXml = args[2];
				File file = new File(insertXml);
				if(!file.exists()){
					System.out.println(CMD_TXT);
					return;
				}
				XmlEditor.addTag(insertXml);
				return;
			}else if("-r".equals(args[1])){
				if(args.length < 4){
					System.out.println(CMD_TXT);
					return;
				}
				String tag = args[2];
				String tagName = args[3];
				XmlEditor.removeTag(tag, tagName);
				return;
			}else{
				System.out.println(CMD_TXT);
				return;
			}
		}else if("-attr".equals(args[0])){
			if(args.length < 2){
				System.out.println(CMD_TXT);
				return;
			}
			if("-i".equals(args[1])){
				if(args.length < 6){
					System.out.println(CMD_TXT);
					return;
				}
				String tag = args[2];
				String tagName = args[3];
				String attr = args[4];
				String value = args[5];
				XmlEditor.addAttr(tag, tagName, attr, value);
				return;
			}else if("-r".equals(args[1])){
				if(args.length < 5){
					System.out.println(CMD_TXT);
					return;
				}
				String tag = args[2];
				String tagName = args[3];
				String attr = args[4];
				XmlEditor.removeAttr(tag, tagName, attr);
				return;
			}else if("-m".equals(args[1])){
				if(args.length < 6){
					System.out.println(CMD_TXT);
					return;
				}
				String tag = args[2];
				String tagName = args[3];
				String attr = args[4];
				String value = args[5];
				XmlEditor.modifyAttr(tag, tagName, attr, value);
			}else{
				System.out.println(CMD_TXT);
				return;
			}
		}
	}
	
}
