# bencinmonitor / d2

High-performance API for [bencinmonitor.si].

## Docker

> TODO: Add instructions here.


## REST Endpoints

### GET /stations

Returns list of stations and prices.

Supported `GET` parameters

- `near`[`String`] - Address or location "name". `near` is geo-coded into coordinates then are then used as starting point.
- `at`[`Double,Double`] - Coordinates ther are used for starting point.
- `maxDistance`[`Int`] - Maximal distance in meters around `near` or `at`. Default is `10000` meters.
- `limit`[`Int`] - Maximum number of records that API returns. Default is `10`.


# Author

- [Oto Brglez](https://github.com/otobrglez)

[bencinmonitor.si]: http://bencinmonitor.si