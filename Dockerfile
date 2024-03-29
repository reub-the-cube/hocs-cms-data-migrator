FROM quay.io/ukhomeofficedigital/hocs-base-image-build as builder

ARG PACKAGE_TOKEN='INVALID'
WORKDIR /builder

COPY . .

RUN PACKAGE_TOKEN=$PACKAGE_TOKEN ./gradlew clean assemble --no-daemon && java -Djarmode=layertools -jar ./build/libs/hocs-*.jar extract

FROM quay.io/ukhomeofficedigital/hocs-base-image

WORKDIR /app

COPY --from=builder --chown=user_hocs:group_hocs ./builder/scripts/run.sh ./
COPY --from=builder --chown=user_hocs:group_hocs ./builder/spring-boot-loader/ ./
COPY --from=builder --chown=user_hocs:group_hocs ./builder/dependencies/ ./
COPY --from=builder --chown=user_hocs:group_hocs ./builder/application/ ./

USER 10000

CMD ["sh", "/app/run.sh"]
