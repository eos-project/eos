FROM maven:3.3.9-jdk-8

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

ADD . /usr/src/app

RUN mvn package
RUN echo "default=default" > /etc/eos-realms.ini

EXPOSE 8087 8090

CMD java -Xmx150m -jar ./target/eos.jar --ws=8090 --udp --realms=/etc/eos-realms.ini
