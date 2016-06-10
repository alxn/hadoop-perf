/**
 * Copyright (c) 2016, Alun Evans.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are NOT permitted.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.badgerous.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Word Counter.
 */
public final class WordCount extends Configured implements Tool {

    /**
     * Ctor.
     */
    private WordCount() {
        super();
    }

    /**
     * Main entry point.
     * @param args Command line arguments
     * @throws Exception If some problem
     */
    public static void main(final String... args) throws Exception {
        System.exit(
            ToolRunner.run(new Configuration(), new WordCount(), args)
        );
    }

    /**
     * Tool Runner.
     * @param args Arguments
     * @return Result of job
     * @throws Exception
     */
    @Override
    public int run(final String... args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException(
                "Expected exactly 2 parameters - Input and Output paths"
            );
        }
        final Path input = new Path(args[0]);
        final Path output = new Path(args[1]);
        final Configuration conf = this.getConf();
        if (conf.getBoolean("net.badgerous.hadoop.bw", false)) {
            conf.set(
                "mapreduce.job.map.output.collector.class",
                BwCollector.class.getCanonicalName()
            );
        }
        final Job job = Job.getInstance(conf, WordCount.class.getName());
        job.setJarByClass(WordCount.class);
        job.setMapperClass(Tokenize.class);
        if (conf.getBoolean("net.badgerous.hadoop.combine", true)) {
            job.setCombinerClass(Aggregate.class);
        }
        job.setReducerClass(Aggregate.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, input);
        FileOutputFormat.setOutputPath(job, output);
        return job.waitForCompletion(true) ? 0 : 1;
    }
}
