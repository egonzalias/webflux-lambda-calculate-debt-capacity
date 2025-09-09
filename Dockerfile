FROM public.ecr.aws/lambda/java:21

COPY build/libs/lambda-send-email.jar /tmp/lambda-send-email.jar

RUN mkdir -p /var/task && cd /var/task && jar xf /tmp/lambda-send-email.jar

CMD ["co.com.crediya.Handler::handleRequest"]
