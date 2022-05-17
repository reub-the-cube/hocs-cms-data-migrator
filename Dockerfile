FROM quay.io/ukhomeofficedigital/hocs-base-image-build as builder

USER root

COPY . .
RUN ./gradlew clean assemble --no-daemon

RUN java -Djarmode=layertools -jar ./build/libs/hocs-cms-data-migrator-0.0.1-SNAPSHOT.jar extract

FROM quay.io/ukhomeofficedigital/hocs-base-image

COPY --from=builder --chown=user_hocs:group_hocs ./scripts/run.sh ./
COPY --from=builder --chown=user_hocs:group_hocs ./spring-boot-loader/ ./
COPY --from=builder --chown=user_hocs:group_hocs ./dependencies/ ./
COPY --from=builder --chown=user_hocs:group_hocs ./application/ ./

CMD ["sh", "/app/run.sh"]
