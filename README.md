# hocs-cms-data-migrator



This is the Home Office Correspondence Systems (HOCS) Data Migrator service. The service migrates the CMS cases to DECS through the use of DECS services. 

## Getting Started


### Prerequisites

* ```Java 17```
* ```Docker```
* ```LocalStack```

### Preparation

In order to run the service locally, LocalStack is required. We have provided an [docker-compose.yml](https://github.com/UKHomeOffice/hocs/tree/main/docker/docker-compose.yml) file to support this. 

To start LocalStack through Docker, run the following command from the root of the project:

```shell
docker-compose up
```

This brings up the LocalStack docker image and creates the necessary AWS resources to run the project. This is done through mounting the [localstack configuration folder](config/localstack) into the docker image.


```shell
docker-compose down
```

## Using the Command Runners

These will be run using Helm and Kubernetes jobs. These instructions will be updated.

### Extract single CMS complaint

CMS_EXTRACT_SINGLE_COMPLAINT=enabled;COMPLAINT_ID=2000000;

### Extract complaints by date range

CMS_EXTRACT_COMPLAINTS=false;COMPLAINT_START_DATE=2022-01-01;COMPLAINT_END_DATE=2022-12-31




### Versioning

For versioning this project uses SemVer.
### Authors

This project is authored by the Home Office.
### License

This project is licensed under the MIT license. For details please see License

This project contains public sector information licensed under the Open Government Licence v3.0. (http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/)
