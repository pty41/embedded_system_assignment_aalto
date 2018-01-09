"""This file contains network handler class."""

import logging
import requests

network_logger = logging.getLogger('raspberry.network')


class NetworkHandler():
    """Class for handling communication between RPi and server."""

    def __init__(self, URL):
        """Init NetworkHandler class."""
        self.address = URL
        self.buffer_loc = "buffer/"
        self.mode = "train"
        self.logger = logging.getLogger('raspberry.network.NetworkHandler')

    def update_mode(self):
        """Update which mode is on."""
        pass

    def post_image(self, filename):
        """Post data to server."""
        URL = self.address + "/api/getPrediction"
        with open(filename, 'rb') as f:
            files = {'file': f}
            try:
                r = requests.post(URL, files=files, timeout=1)
            except Exception as e:
                self.logger.error(e)
                return False
        if r.json()["Status"] == "Success":
            self.logger.info(r.json()["msg"])
            try:
                value = int(r.json()["value"])
                return value
            except Exception as e:
                self.logger.error(e)
                return False
        else:
            self.logger.error(r.json()["msg"])

    def poll_server(self, *args, **kwargs):
        """Poll server for data."""
        pass

    def get_mode(self):
        """Return which mode is on."""
        return self.mode
