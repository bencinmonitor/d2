# bencinmonitor / d2

High-performance API for [bencinmonitor.si].

[![Build Status](https://travis-ci.org/bencinmonitor/d2.svg?branch=master)](https://travis-ci.org/bencinmonitor/d2)

## REST Endpoints

### GET /stations

Returns list of stations and prices.

Supported `GET` parameters

- `near`[`String`] - Address or location "name". `near` is geo-coded into coordinates then are then used as starting point.
- `at`[`Double,Double`] - Coordinates ther are used for starting point.
- `maxDistance`[`Int`] - Maximal distance in meters around `near` or `at`. Default is `10000` meters.
- `limit`[`Int`] - Maximum number of records that API returns. Default is `10`.

## Docker

```bash
sbt assembly

docker build -t bencinmonitor/d2:latest .

docker run -ti --rm \
    -e MONGO_URI=mongodb://10.8.8.8:27017/bm \
    -e REDIS_URI=redis://@10.8.8.8:6379/1 \
    -p 0.0.0.0:4444:9000 bencinmonitor/d2:latest
```


# Author

- [Oto Brglez](https://github.com/otobrglez)

[bencinmonitor.si]: http://bencinmonitor.si