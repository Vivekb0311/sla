#FROM alpine
#RUN apk --update add  procps
#RUN apk add openjdk17 curl bash procps
#RUN apk add --no-cache bash
#RUN apk add curl
#RUN apk add ttf-dejavu
#Run fc-cache --force
#ENV JAVA_HOME /usr/lib/jvm/java-17-openjdk
#ENV PATH $PATH:/usr/lib/jvm/java-17-openjdk/bin

FROM eclipse-temurin:17-alpine
RUN apk add curl bash

# Create a non-root user and group
RUN addgroup -S bootnext && adduser -S -G bootnext bootnext

ENV SERVICE_ARCHIVE sla
ENV SERVICE_PATH /opt/bootnext/$SERVICE_ARCHIVE


WORKDIR $SERVICE_PATH
ENV BASE_PATH /opt/bootnext/
RUN mkdir -p $SERVICE_PATH

ADD ./$SERVICE_ARCHIVE.tar $SERVICE_PATH

Copy --chmod=407 ./melodyposthook.sh /opt/
# Set ownership for the service path
RUN chown -R bootnext:bootnext $SERVICE_PATH

# CMD
USER bootnext

CMD bash run.sh start ; sleep 120s ; tail -f logs/service.log
