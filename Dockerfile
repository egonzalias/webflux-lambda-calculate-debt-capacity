FROM public.ecr.aws/lambda/java:21

COPY build/libs/lambda-calculate-debt-capacity.jar /tmp/lambda-send-email.jar

RUN mkdir -p /var/task && cd /var/task && jar xf /tmp/lambda-calculate-debt-capacity.jar

CMD ["co.com.crediya.entrypoints.Handler::handleRequest"]
