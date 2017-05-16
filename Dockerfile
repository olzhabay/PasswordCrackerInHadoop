FROM olzhabay/hadoop-compiled

MAINTAINER olzhabay <olzhabay.i@gmail.com>

RUN mkdir -p /root/PasswordCrackerInHadoop/classes
COPY src /root/PasswordCrackerInHadoop/src/.
COPY run.sh /root/PasswordCrackerInHadoop/.
COPY compile.sh /root/PasswordCrackerInHadoop/.
RUN cd /root/PasswordCrackerInHadoop/ && \
    sh /root/PasswordCrackerInHadoop/compile.sh
