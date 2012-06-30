
my $slurp;
{
    local $/ = undef;
    open my $textfile, '<', $ARGV[0] or die $!;
    $slurp = <$textfile>;
    close $textfile;
}

while( $slurp =~ m/<phon(?:s|ology)>\s*-?([^<-]+)-?\s*<\/pho/g ) {
    print "$1\n";
}
