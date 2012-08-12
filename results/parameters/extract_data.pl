package perltools;
use strict;
use warnings;
use File::Find::Rule;
use Data::Dumper;

my $dir = exists($ARGV[1]) ? $ARGV[1] : "./";

my @files = File::Find::Rule->file()
							->name( qr/$ARGV[0]/)
							->maxdepth(1)
							->in ($dir);

@files= sort(@files);

foreach my $f( @files ){
	$f =~ /=([.0-9]+)/;
	
	my $x = $1;
	my $analyse = $f =~ /SINGLE/ ? "singles" : "pairs";
	
	my $res = `perl ../analyse_$analyse.pl $f | wc -l`;
	
	print "$x $res";
}
