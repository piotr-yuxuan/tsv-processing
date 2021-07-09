# `piotr-yuxuan/tsv-processing`

![](./doc/social-media-preview.jpg)

Data manipulation playground for fun and profit.

[![Clojars badge](https://img.shields.io/clojars/v/com.github.piotr-yuxuan/tsv-processing.svg)](https://clojars.org/com.github.piotr-yuxuan/tsv-processing)
[![cljdoc badge](https://cljdoc.org/badge/com.github.piotr-yuxuan/tsv-processing)](https://cljdoc.org/d/com.github.piotr-yuxuan/tsv-processing/CURRENT)
[![GitHub license](https://img.shields.io/github/license/piotr-yuxuan/tsv-processing)](https://github.com/piotr-yuxuan/tsv-processing/blob/main/LICENSE)
[![GitHub issues](https://img.shields.io/github/issues/piotr-yuxuan/tsv-processing)](https://github.com/piotr-yuxuan/tsv-processing/issues)

# What it offers

`jq` is probably enough to do some tsv file parsing, but this project
tries to be more scalable – at the expense of simplicity.

Some tsv files are processed and the output itself also is written on
disk as a tsv file. Data go throughout different steps:

- Acquisition
- Ingestion (filtering, coercion, anomaly detection)
- Enrichment
- Projection
- Storage

# Maturity and evolution

# Getting started

Clean local Docker environments:

``` zsh
echo y | docker rm --force $(docker ps --all --quiet --filter status=exited)
echo y | docker volume prune
```

Start Docker environment containing local services:

``` zsh
docker compose up
```

In a REPL, start, stop, or restart the streams:

``` clojure
(user-start)

(do (user-stop)
    (user-start))
```

Send records to topic from the command line:

``` zsh
kafka-console-producer \
  --broker-list localhost:9092 \
  --topic "local.tsv-processing.tsv-line.edn" \
  --property parse.key=true \
  --property "key.separator=@"

> key@value
```

Consume input topic records:

``` zsh
kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic "local.tsv-processing.tsv-line.edn" \
  --property print.key=true \
  --from-beginning
```

Consume output topic records:

``` zsh
kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic "local.tsv-processing.output.edn" \
  --property print.key=true \
  --from-beginning
```
