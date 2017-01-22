
FROM maven

RUN mkdir /usr/local/eos
RUN apt-get update \
  && apt-get install -y git make
RUN cd /usr/local/eos \
  && git clone https://github.com/eos-project/eos.git . \
  && make release
RUN echo "eos=eos" > /usr/local/eos/realms.ini

EXPOSE 8087/udp 8090

ENTRYPOINT java -jar /usr/local/eos/release/eos.jar --udp --ws --realms=/usr/local/eos/realms.ini
