FROM java:8

# dot for images
RUN apt-get update && apt-get install -y graphviz && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /usr/local/snowglobe/
RUN mkdir -p /var/snowglobe/


ADD snowglobe.jar /usr/local/snowglobe/
ADD run.sh /usr/local/snowglobe/

RUN chmod -R a+rw /usr/local/snowglobe

RUN ls -l /usr/local/snowglobe

ENV repositoryRoot /var/snowglobe

#expose so it can survive restarts
VOLUME /var/snowglobe
VOLUME /var/run/docker.sock

EXPOSE 8808

CMD ["/bin/bash", "/usr/local/snowglobe/run.sh"]