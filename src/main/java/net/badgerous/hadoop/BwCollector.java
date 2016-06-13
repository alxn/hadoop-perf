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

import com.google.common.io.CountingOutputStream;
import com.google.common.io.NullOutputStream;
import java.io.Closeable;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import net.badgerous.CircularOutputStream;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.Counters;
import org.apache.hadoop.mapred.MapOutputCollector;
import org.apache.hadoop.mapred.MapTask;
import org.apache.hadoop.mapreduce.TaskCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Measure the bandwidth of the collector.
 */
public class BwCollector
    implements MapOutputCollector<WritableComparable<?>, Writable>, Closeable {
    /**
     * When init ran.
     */
    private transient long start;

    /**
     * Slow path output.
     */
    private transient MapTask.MapOutputBuffer<WritableComparable<?>, Writable> hpout;

    /**
     * Counter for map output bytes.
     */
    private transient Counters.Counter output;

    /**
     * How much data we serialized.
     */
    private transient CountingOutputStream counted;

    /**
     * Hadoop's requirement.
     */
    private transient DataOutput dos;

    /**
     * Whether to send to hadoop.
     */
    private transient boolean bypass;

    /**
     * Whether to bitbucket drop the data.
     */
    private transient boolean bitbucket;

    /**
     * Logging.
     */
    private final transient Logger logger;

    /**
     * Ctor.
     */
    public BwCollector() {
        this(
            new MapTask.MapOutputBuffer<WritableComparable<?>, Writable>()
        );
    }

    /**
     * Constructor.
     * @param hpo Hadoop collector
     */
    private BwCollector(
        final MapTask.MapOutputBuffer<WritableComparable<?>, Writable> hpo
    ) {
        this.hpout = hpo;
        this.logger = LoggerFactory.getLogger(BwCollector.class);
    }

    @Override
    public final void init(
        final MapOutputCollector.Context context
    ) throws IOException, ClassNotFoundException {
        this.hpout.init(context);
        this.output = context
            .getReporter()
            .getCounter(TaskCounter.MAP_OUTPUT_BYTES);
        this.bypass = context
            .getJobConf()
            .getBoolean("net.badgerous.hadoop.bypass", false);
        this.bitbucket = context
            .getJobConf()
            .getBoolean("net.badgerous.hadoop.bitbucket", false);
        if (this.bypass) {
            final OutputStream output;
            if (this.bitbucket) {
                output = new NullOutputStream();
            } else {
                output = new CircularOutputStream();
            }
            this.counted = new CountingOutputStream(output);
            this.dos = new DataOutputStream(this.counted);
        }
        this.logger.info("init complete");
        this.start = System.currentTimeMillis();
    }

    @Override
    public final void collect(
        final WritableComparable<?> key, final Writable value,
        final int partition
    ) throws IOException {
        if (this.bypass) {
            key.write(this.dos);
            value.write(this.dos);
            this.dos.write(partition);
        } else {
            this.hpout.collect(key, value, partition);
        }
    }

    @Override
    public final void close() throws IOException {
        final long end = System.currentTimeMillis();
        this.counted.close();
        this.hpout.close();
        final long bytes;
        if (this.bypass) {
            bytes = this.counted.getCount();
        } else {
            bytes = this.output.getCounter();
        }
        this.logger.info(
            String.format(
                "Bypass: %b BitBucket:%b Sent %d bytes in %ds => %g Mbps",
                this.bypass, this.bitbucket, bytes,
                TimeUnit.MILLISECONDS.toSeconds(end - this.start),
                (double)(bytes / (end-this.start))
                    * (double) TimeUnit.SECONDS.toMillis(1L)
                    / 1024.0 / 1024.0 * 8.0
            )
        );
        this.logger.info("close complete");
    }

    @Override
    public final void flush() throws InterruptedException, IOException, ClassNotFoundException {
        this.hpout.flush();
    }
}
