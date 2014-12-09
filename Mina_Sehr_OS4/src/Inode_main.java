import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;


public class Inode_main {

	public static void main(String[] args) {
		//check for both input files from the command line: input_file, file_access_trace.
		if (args.length==2){
			File input_file = new File(args[0]);
			File file_access_trace = new File(args[1]);
			//further process the files
			processFiles(input_file, file_access_trace);
		}
		else{
			System.out.println("Need two input files from the command line: input_file, file_access_trace.");
			System.exit(0);
		}

	}
	
	private static void processFiles(File input_file, File file_access_trace){
		System.out.println("processing files");
		try{
			// read the input file
			Scanner input_scanner=new Scanner(input_file);
			readInputScanner(input_scanner);
			input_scanner.close();
			
				try{
					// read he file access trace
					Scanner file_trace_scanner=new Scanner(file_access_trace);
					readAccessScanner(file_trace_scanner);
					file_trace_scanner.close();
					}
					catch(FileNotFoundException file_excep){
						System.out.println("File access trace not found");
					}
				}
			catch(FileNotFoundException file_excep){
				System.out.println("Input file not found");
			}
	}
	
	private static void readInputScanner(Scanner input_scanner){
		
		// store text file names for super_block.txt
		ArrayList<String> text_file_names=new ArrayList<String>();
		
		// file names for single_indirect_block and 
		String single_indirect_filename="single_indirect_block.txt";
		String double_indirect_filename="double_indirect_block.txt";
		
		// keep track of line number
		int inode_point_num=0;
		
		// first line only has header
		if (input_scanner.hasNextLine())
			input_scanner.nextLine();
		
		while(input_scanner.hasNextLine()){
			inode_point_num++;
			if (inode_point_num==13){
				createSingleIndirectBlock(single_indirect_filename, input_scanner);
				text_file_names.add(single_indirect_filename);
			}
			else if(inode_point_num==113){
				createDoubleIndirectBlock_First(double_indirect_filename, input_scanner);
				text_file_names.add(double_indirect_filename);
			}
			else if(inode_point_num==213){
				createDoubleIndirectBlock_Second(double_indirect_filename, input_scanner);
			}
			else if (inode_point_num<13){
				String line=input_scanner.nextLine();
				String[] words=line.split(",");
				int block_num=Integer.parseInt(words[0]);
				String data=words[1];
				System.out.println("CREATING block_num: "+block_num+" data: "+data);
				String file_name=String.valueOf(block_num)+".txt";
				text_file_names.add(file_name);
				createBlock(file_name, data);
			}
		}
		addSuperBlock(text_file_names.toArray(new String[text_file_names.size()]));
	}
	
	private static void createDoubleIndirectBlock_First(
			String double_indirect_filename, Scanner input_scanner) {
		String double_first_file="double_indirect_first.txt";
		createSingleIndirectBlock(double_first_file, input_scanner);
		createBlock(double_indirect_filename, double_first_file);
	}
	
	private static void createDoubleIndirectBlock_Second(
			String double_indirect_filename, Scanner input_scanner) {
		String double_second_file="double_indirect_second.txt";
		createSingleIndirectBlock(double_second_file, input_scanner);
		createBlock(double_indirect_filename, "double_indirect_first.txt"+"\n"+double_second_file);
	}

	private static void createSingleIndirectBlock(
			String single_indirect_filename, Scanner input_scanner) {
		ArrayList<String> text_file_names=new ArrayList<String>();
		int num_entries=0;
		while(num_entries<100 && input_scanner.hasNextLine()){
			String line=input_scanner.nextLine();
			String[] words=line.split(",");
			int block_num=Integer.parseInt(words[0]);
			String data=words[1];
			System.out.println("CREATING block_num: "+block_num+" data: "+data);
			String file_name=String.valueOf(block_num)+".txt";
			text_file_names.add(file_name);
			createBlock(file_name, data);
			num_entries++;
		}
		String data="";
		String[] text_files=text_file_names.toArray(new String[text_file_names.size()]);
		for (int i=0;i<text_files.length;i++){
			data+=text_files[i];
			if (i!= text_files.length-1){
				data+="\n";
			}
		}
		createBlock(single_indirect_filename, data);
		System.out.println("ADDED single indirect file");
	}

	private static void addSuperBlock(String[] text_file_names){
		// add test_file.txt as superblock
		String data="";
		
		//convert the array of file names to printable names
		for (int i=0;i<text_file_names.length;i++){
			data+=text_file_names[i];
			if (i!= text_file_names.length-1){
				data+="\n";
			}
		}
		createBlock("test_file.txt", data);
		System.out.println("ADDED superblock with data: "+data);
	}
	
	private static void createBlock(String file_name, String data){
		PrintWriter writer;
		try {
			writer = new PrintWriter("input_file/"+file_name);
			writer.println(data);
			writer.close();
			System.out.println("ADDED filename: "+file_name+ "with data: "+data);
		} catch (FileNotFoundException e) {
			System.out.println("Failed to create file");
			e.printStackTrace();
		}
		
	}
	
	private static void readAccessScanner(Scanner input_scanner){
		
		// first line only has 
		if (input_scanner.hasNextLine())
			input_scanner.nextLine();
		while(input_scanner.hasNextLine()){
			String line=input_scanner.nextLine();
			String[] words=line.split(", ");
			String command=words[0];
			int block_num=Integer.parseInt(words[1]);
			if (words.length==3 && command.equals("W")){
				write(block_num, words[2]);
			}
			else{
				read(block_num);
			}
		}
	}
	
	private static void write(int block_num, String data){	
		String file_name=String.valueOf(block_num)+".txt";
		PrintWriter writer;
		try {
			writer = new PrintWriter("input_file/"+file_name);
			System.out.println("WRITING block_num: "+block_num+" data: "+data);
			writer.println(data);
			writer.close();
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't write");
			e.printStackTrace();
		}
	}
	
	private static void read(int block_num){
		String file_name=get_file_name(block_num);
		if (file_name.isEmpty()){
			System.out.println("Block: "+block_num+" does not exist");
			return;
		}
			
		File read_file = new File("input_file/"+file_name);
		try{
			Scanner read_scanner=new Scanner(read_file);
			String data_read="";
			while(read_scanner.hasNextLine()){
				data_read+=read_scanner.nextLine();
				System.out.println(data_read);
				}
			read_scanner.close();
			System.out.println("READ block_num: "+block_num+" with data: "+data_read);
			}
			catch(FileNotFoundException file_excep){
				System.out.println("File name: "+ file_name+" not found for reading");
			}
	}

	private static String get_file_name(int block_num) {
		String file_name="";
		if(block_num<12){
			System.out.println("READING input_file for block: "+block_num);
			File read_file = new File("input_file/test_file.txt");
			try {
				int line=0;
				Scanner read_scanner=new Scanner(read_file);
				while(read_scanner.hasNextLine() && line<=block_num){
					if(line==block_num){
						file_name=read_scanner.nextLine();
					}
					else{
						read_scanner.nextLine();
					}
					line++;
				}
				read_scanner.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		else if (block_num<112){
			String indirect_single=getLineNum("test_file.txt",12);
			if (!indirect_single.isEmpty()){
				System.out.println("READING single indirect block for block: "+block_num);
				file_name=getLineNum(indirect_single,block_num-12);
			}
		}
		else if(block_num<312){
			String indirect_double=getLineNum("test_file.txt",12);
			if (!indirect_double.isEmpty())
			{
				System.out.println("READING double indirect block for block: "+block_num);
			if(block_num<212){
				String file_read=getLineNum(indirect_double,0);
				System.out.println("READING "+file_read+" from double indirect block");
				file_name=getLineNum(file_read, block_num-12-100);
			}
			else{
				String file_read=getLineNum(indirect_double,0);
				if (!file_read.isEmpty()){
					System.out.println("READING "+file_read+" from double indirect block");
					file_name=getLineNum(file_read, block_num-12-200);
				}
			}
			}
		}
		return file_name;
	}

	private static String getLineNum(String fileName, int line_index) {
		String file_name="";
		File read_file = new File("input_file/"+fileName);
		try {
			int line=0;
			Scanner read_scanner=new Scanner(read_file);
			while(read_scanner.hasNextLine() && line<=line_index){
				if(line==line_index){
					file_name=read_scanner.nextLine();
				}
				else{
					read_scanner.nextLine();
				}
				line++;
			}
			read_scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return file_name;
	}

}
