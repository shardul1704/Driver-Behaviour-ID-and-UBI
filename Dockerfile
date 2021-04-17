FROM continuumio/anaconda3:4.4.0
COPY . /usr/app/
EXPOSE 5000
WORKDIR /usr/app/
RUN pip install -r requirements.txt
ENTRYPOINT [ "flask"]
CMD [ "run", "--host", "0.0.0.0" ]