# A minimal Nginx container including ContainerPilot
FROM nginx:1.13

# Add some stuff via apt-get
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        bc \
        ca-certificates \
        curl \
        unzip \
    && rm -rf /var/lib/apt/lists/*


# get ContainerPilot release
ENV CONTAINERPILOT_VERSION 3.7.0
RUN export CP_SHA1=b10b30851de1ae1c095d5f253d12ce8fe8e7be17 \
    && curl -Lso /tmp/containerpilot.tar.gz \
         "https://github.com/joyent/containerpilot/releases/download/${CONTAINERPILOT_VERSION}/containerpilot-${CONTAINERPILOT_VERSION}.tar.gz" \
    && echo "${CP_SHA1}  /tmp/containerpilot.tar.gz" | sha1sum -c \
    && tar zxf /tmp/containerpilot.tar.gz -C /bin \
    && rm /tmp/containerpilot.tar.gz

# Install Consul
# Releases at https://releases.hashicorp.com/consul
RUN export CONSUL_VERSION=1.0.7 \
    && export CONSUL_CHECKSUM=6c2c8f6f5f91dcff845f1b2ce8a29bd230c11397c448ce85aae6dacd68aa4c14 \
    && curl --retry 7 --fail -vo /tmp/consul.zip "https://releases.hashicorp.com/consul/${CONSUL_VERSION}/consul_${CONSUL_VERSION}_linux_amd64.zip" \
    && echo "${CONSUL_CHECKSUM}  /tmp/consul.zip" | sha256sum -c \
    && unzip /tmp/consul -d /usr/local/bin \
    && rm /tmp/consul.zip \
    && mkdir /config \
    && mkdir /etc/consul \
    && mkdir /var/lib/consul 


# Install Consul template
# Releases at https://releases.hashicorp.com/consul-template/
RUN export CONSUL_TEMPLATE_VERSION=0.19.4 \
    && export CONSUL_TEMPLATE_CHECKSUM=5f70a7fb626ea8c332487c491924e0a2d594637de709e5b430ecffc83088abc0 \
    && curl --retry 7 --fail -Lso /tmp/consul-template.zip "https://releases.hashicorp.com/consul-template/${CONSUL_TEMPLATE_VERSION}/consul-template_${CONSUL_TEMPLATE_VERSION}_linux_amd64.zip" \
    && echo "${CONSUL_TEMPLATE_CHECKSUM}  /tmp/consul-template.zip" | sha256sum -c \
    && unzip /tmp/consul-template.zip -d /usr/local/bin \
    && rm /tmp/consul-template.zip


################################################################################

# Add Dehydrated
RUN export DEHYDRATED_VERSION=v0.3.1 \
    && curl --retry 8 --fail -Lso /tmp/dehydrated.tar.gz "https://github.com/lukas2511/dehydrated/archive/${DEHYDRATED_VERSION}.tar.gz" \
    && tar xzf /tmp/dehydrated.tar.gz -C /tmp \
    && mv /tmp/dehydrated-0.3.1/dehydrated /usr/local/bin \
    && rm -rf /tmp/dehydrated-0.3.1

# Add jq
RUN export JQ_VERSION=1.5 \
    && curl --retry 8 --fail -Lso /usr/local/bin/jq "https://github.com/stedolan/jq/releases/download/jq-${JQ_VERSION}/jq-linux64" \
    && chmod a+x /usr/local/bin/jq

# Add our configuration files and scripts
RUN rm -f /etc/nginx/conf.d/default.conf
#COPY etc/acme /etc/acme
COPY etc/containerpilot.json5 /etc/
COPY etc/nginx /etc/nginx
COPY bin /usr/local/bin



# Usable SSL certs written here
RUN mkdir -p /var/www/ssl
# Temporary/work space for keys
RUN mkdir -p /var/www/acme/ssl
# ACME challenge tokens written here
RUN mkdir -p /var/www/acme/challenge
# Consul session data written here
RUN mkdir -p /var/consul

EXPOSE 80
EXPOSE 8500

ENV CONTAINERPILOT /etc/containerpilot.json5

CMD [ "/bin/containerpilot"]
