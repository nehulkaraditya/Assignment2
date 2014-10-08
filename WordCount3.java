import java.io.IOException;
import java.util.*;
        
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Map;

public class WordCount {

 public static  PriorityQueue<String[]> pq;
              
 public static class Map extends Mapper<LongWritable, Text, Text, IntWritable> {
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
        
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        StringTokenizer tokenizer = new StringTokenizer(line);
        while (tokenizer.hasMoreTokens()) {
	    String temp = tokenizer.nextToken();
	    if(temp.length() == 7){
		word.set(temp);
            	context.write(word, one);
	    }
	            
	}
    }
 } 
        
 public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable> {
    public void reduce(Text key, Iterable<IntWritable> values, Context context) 
      throws IOException, InterruptedException {
        int sum = 0;
	
        for (IntWritable val : values) {
            sum += val.get();
        }
        
	String temp[] = pq.peek();
        int val = Integer.parseInt(temp[1]);
        if(sum>val){
            pq.poll();
            String[] ob = new String[2];
            ob[0] = key.toString();
            ob[1] = String.valueOf(sum);
	    pq.add(ob);
        }
        

        context.write(key, new IntWritable(sum));
    }
 }

 public static class KeyValComparator implements Comparator<String[]>{
    @Override
    public int compare(String[] x, String[] y){
        int a = Integer.parseInt(x[1]),b = Integer.parseInt(y[1]);
        return a<b?-1:a>b?1:0;
    }
 }   
 public static void main(String[] args) throws Exception {

	KeyValComparator kvc = new KeyValComparator();
	pq= new PriorityQueue<>(100,kvc);
	
	for(int i=0;i<100;i++){
		String[] s = new String[2];
		s[0] = "";
		s[1] = "0";
		pq.add(s);
	}
	Configuration conf = new Configuration();

	Job job = new Job(conf, "wordcount");

	job.setOutputKeyClass(Text.class);
	job.setOutputValueClass(IntWritable.class);

	job.setMapperClass(Map.class);
	job.setReducerClass(Reduce.class);

	job.setInputFormatClass(TextInputFormat.class);
	job.setOutputFormatClass(TextOutputFormat.class);

	job.setNumReduceTasks(1);
	job.setJarByClass(WordCount.class);

	FileInputFormat.addInputPath(job, new Path(args[0]));
	FileOutputFormat.setOutputPath(job, new Path(args[1]));

	job.waitForCompletion(true);
	
	BufferedWriter bw = new BufferedWriter(new FileWriter(args[1] + "//output1.txt"));
	Stack<String[]> s = new Stack<String[]>();
	
	while(!pq.isEmpty()){
		s.push(pq.poll());
	}
	
	while(!s.isEmpty()){
		String t1[] = s.pop();
		
		bw.write(t1[0].toCharArray());
		bw.append('\t');
		bw.write(t1[1].toCharArray());
		bw.append('\n');
	}
	bw.close();

 }
        
}
