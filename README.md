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

These will be run using Helm and Kubernetes jobs. These instructions will be updated further.
Each runner / extractor will need an environment variable set to `enabled` along with the example usages below
Expected use case for deployment would have one extractor enabled at a time.

### Extract multiple specfic CMS complaints

```yaml
- name: CMS_EXTRACT_COMPLAINTS
  value: 'disabled'
- name: CMS_EXTRACT_OPEN_COMPLAINTS
  value: 'disabled'
- name: CMS_EXTRACT_CLOSED_COMPLAINTS
  value: 'disabled'
- name: CMS_EXTRACT_MULTIPLE_COMPLAINTS
  value: 'enabled'
- name: COMPLAINT_START_DATE
  value: ''                                 # Not relevant for this extractor
- name: COMPLAINT_END_DATE
  value: ''                                 # Not relevant for this extractor
- name: COMPLAINT_IDS
  value: '{TO BE SET}'                      # Pipe delimited list of CMS Case IDs e.g. 2000001|2000002|2000003
- name: SEND_MIGRATION_MESSAGE              
  value: 'enabled'                          # Anything other than 'enabled' will extract only and prevent the message from being sent to DECS
- name: MIGRATION_DOCUMENT                  
  value: 'enabled'                          # Anything other than 'enabled' will prevent the case data PDF from being generated for the extracted case
```

### Extract complaints by date range

```yaml
- name: CMS_EXTRACT_COMPLAINTS
  value: 'enabled'
- name: CMS_EXTRACT_OPEN_COMPLAINTS
  value: 'disabled'
- name: CMS_EXTRACT_CLOSED_COMPLAINTS
  value: 'disabled'
- name: CMS_EXTRACT_MULTIPLE_COMPLAINTS
  value: 'disabled'
- name: COMPLAINT_START_DATE
  value: '{TO BE SET}'                      # Inclusive start date of complaints to be extracted in yyyy-mm-dd format e.g. 2022-01-01
- name: COMPLAINT_END_DATE
  value: '{TO BE SET}'                      # Inclusive end date of complaints to be extracted in yyyy-mm-dd format e.g. 2022-12-31
- name: COMPLAINT_IDS
  value: ''                                 # Not relevant for this runner
- name: SEND_MIGRATION_MESSAGE              
  value: 'enabled'                          # Anything other than 'enabled' will extract only and prevent the message from being sent to DECS
- name: MIGRATION_DOCUMENT                  
  value: 'enabled'                          # Anything other than 'enabled' will prevent the case data PDF from being generated for the extracted case
```

### Extract closed complaints by date range

```yaml
- name: CMS_EXTRACT_COMPLAINTS
  value: 'disabled'
- name: CMS_EXTRACT_OPEN_COMPLAINTS
  value: 'disabled'
- name: CMS_EXTRACT_CLOSED_COMPLAINTS
  value: 'enabled'
- name: CMS_EXTRACT_MULTIPLE_COMPLAINTS
  value: 'disabled'
- name: COMPLAINT_START_DATE
  value: '{TO BE SET}'                      # Inclusive start date of complaints to be extracted in yyyy-mm-dd format e.g. 2022-01-01
- name: COMPLAINT_END_DATE
  value: '{TO BE SET}'                      # Inclusive end date of complaints to be extracted in yyyy-mm-dd format e.g. 2022-12-31
- name: COMPLAINT_IDS
  value: ''                                 # Not relevant for this runner
- name: SEND_MIGRATION_MESSAGE              
  value: 'enabled'                          # Anything other than 'enabled' will extract only and prevent the message from being sent to DECS
- name: MIGRATION_DOCUMENT                  
  value: 'enabled'                          # Anything other than 'enabled' will prevent the case data PDF from being generated for the extracted case
```

### Extract open complaints by date range

```yaml
- name: CMS_EXTRACT_COMPLAINTS
  value: 'disabled'
- name: CMS_EXTRACT_OPEN_COMPLAINTS
  value: 'enabled'
- name: CMS_EXTRACT_CLOSED_COMPLAINTS
  value: 'disabled'
- name: CMS_EXTRACT_MULTIPLE_COMPLAINTS
  value: 'disabled'
- name: COMPLAINT_START_DATE
  value: '{TO BE SET}'                      # Inclusive start date of complaints to be extracted in yyyy-mm-dd format e.g. 2022-01-01
- name: COMPLAINT_END_DATE
  value: '{TO BE SET}'                      # Inclusive end date of complaints to be extracted in yyyy-mm-dd format e.g. 2022-12-31
- name: COMPLAINT_IDS
  value: ''                                 # Not relevant for this runner
- name: SEND_MIGRATION_MESSAGE              
  value: 'enabled'                          # Anything other than 'enabled' will extract only and prevent the message from being sent to DECS
- name: MIGRATION_DOCUMENT                  
  value: 'enabled'                          # Anything other than 'enabled' will prevent the case data PDF from being generated for the extracted case
```


### Versioning

For versioning this project uses SemVer.
### Authors

This project is authored by the Home Office.
### License

This project is licensed under the MIT license. For details please see License

This project contains public sector information licensed under the Open Government Licence v3.0. (http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/)
