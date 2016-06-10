Hadoop Performance Testing
==========================

Run the job the normal way:

    $ hadoop jar metrics-1.0-SNAPSHOT.jar [flags] <input> <output>

In addition, there are 3 flags that can be specified to alter the behaviour:

1. Disable the Combiner (enabled by default)

        -Dnet.badgerous.hadoop.combine=false

2. Decorate the Hadoop Collector to log bandwidth (disabled by default): 

        -Dnet.badgerous.hadoop.bw=true

    * Optionally calculate maximum theoretical bandwidth of serialization to a
      bitbucket:

            -Dnet.badgerous.hadoop.bitbucket=true


Results
-------

Running with input data generated via [HiBench], and with:

    -Dnet.badgerous.hadoop.bw=true
    -Dmapreduce.job.reduces=1

1. Combiner on:

        BitBucket:false Sent 25631227 bytes in 3s => 50.0336 Mbps
        BitBucket:false Sent 1526907199 bytes in 231s => 50.2548 Mbps

2. Combiner off: 

        BitBucket:false Sent 25631227 bytes in 4s => 48.6526 Mbps
        BitBucket:false Sent 1526907199 bytes in 322s => 36.1710 Mbps

3. Combiner don't care, bit-bucket on:

        BitBucket:true Sent 27322312 bytes in 1s => 197.960 Mbps
        BitBucket:true Sent 1627760516 bytes in 44s => 282.227 Mbps

[HiBench]: https://github.com/intel-hadoop/HiBench
