FROM quay.io/ukhomeofficedigital/hocs-base-image-build as builder

ARG PACKAGE_TOKEN='INVALID'

COPY . .

RUN PACKAGE_TOKEN=$PACKAGE_TOKEN ./gradlew clean assemble --no-daemon && java -Djarmode=layertools -jar ./build/libs/hocs-cms-data-migrator.jar extract

FROM quay.io/ukhomeofficedigital/hocs-base-image

WORKDIR /app

COPY --from=builder --chown=user_hocs:group_hocs ./scripts/run.sh ./
COPY --from=builder --chown=user_hocs:group_hocs ./spring-boot-loader/ ./
COPY --from=builder --chown=user_hocs:group_hocs ./dependencies/ ./
COPY --from=builder --chown=user_hocs:group_hocs ./application/ ./

USER 10000

CMD ["sh", "/app/run.sh"]
