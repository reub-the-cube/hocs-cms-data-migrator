{{- define "deployment.envs" }}
- name: JAVA_OPTS
  value: '-Xms768m -Xmx768m -Dlogging.level.uk.gov.digital.ho.hocs.cms=INFO -Djavax.net.ssl.trustStore=/etc/keystore/truststore.jks -Dhttps.proxyHost=hocs-outbound-proxy.{{ .Release.Namespace }}.svc.cluster.local -Dhttps.proxyPort=31290 -Dhttp.nonProxyHosts=*.{{ .Release.Namespace }}.svc.cluster.local'
- name: SPRING_PROFILES_ACTIVE
  value: 'sqs, s3'
- name: CMS_DB_HOST
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-mssql
      key: endpoint
- name: CMS_DB_PORT
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-mssql
      key: port
- name: CMS_DB_NAME
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-mssql
      key: default_db
- name: CMS_DB_USERNAME
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-mssql
      key: username
- name: CMS_DB_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-mssql
      key: password
- name: POSTGRES_DB_HOST
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-postgres
      key: host
- name: POSTGRES_DB_PORT
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-postgres
      key: port
- name: POSTGRES_DB_NAME
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-postgres
      key: name
- name: POSTGRES_DB_SCHEMA_NAME
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-postgres
      key: schema_name
- name: POSTGRES_DB_USERNAME
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-postgres
      key: user_name
- name: POSTGRES_DB_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-postgres
      key: password
- name: AWS_S3_BUCKET_NAME
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-untrusted-s3
      key: bucket_name
- name: AWS_S3_UNTRUSTED_ACCOUNT_ACCESS_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-untrusted-s3
      key: access_key_id
- name: AWS_S3_UNTRUSTED_ACCOUNT_SECRET_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-untrusted-s3
      key: secret_access_key
- name: AWS_S3_UNTRUSTED_ACCOUNT_KMS_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-untrusted-s3
      key: kms_key_id
- name: AWS_SQS_QUEUE_NAME
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-case-migrator-sqs
      key: name
- name: AWS_SQS_ACCESS_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-case-migrator-sqs
      key: access_key_id
- name: AWS_SQS_SECRET_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-case-migrator-sqs
      key: secret_access_key
- name: AWS_SQS_URL
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-case-migrator-sqs
      key: sqs_url
- name: SEND_MIGRATION_MESSAGE
  value: 'enabled'
- name: CMS_EXTRACT_COMPLAINTS
  value: 'disabled'
- name: COMPLAINT_START_DATE
  value: '2022-01-01'
- name: COMPLAINT_END_DATE
  value: '2023-12-31'
- name: CMS_EXTRACT_SINGLE_COMPLAINT
  value: 'enabled'
- name: COMPLAINT_ID
  value: '131000000515'
- name: COMPLAINT_EXTRACT_TYPE
  value: 'ALL_CASES'
{{- end -}}
