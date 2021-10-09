#!/usr/bin/env ruby

require 'webrick'
require 'webrick/httpproxy'
require 'uri'

s = WEBrick::HTTPProxyServer.new({
  :BindAddress => '127.0.0.1',
  :Port => 8080,
  :Logger => WEBrick::Log::new("log.txt", WEBrick::Log::DEBUG),
  :ProxyVia => false,
  :ProxyURI => URI.parse('http://api.treasure-data.com:80/')
})

Signal.trap('INT') do
  s.shutdown
end

s.start
